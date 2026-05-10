import http from 'k6/http';
import { check, sleep } from 'k6';

// Load configuration
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const AUTH_TOKEN = __ENV.AUTH_TOKEN || 'token123';
const USER_ID = __ENV.USER_ID || 'user01';
const PRODUCT_ID = __ENV.PRODUCT_ID || 'P010';
const THINK_TIME_SECONDS = Number(__ENV.THINK_TIME_SECONDS || '1');
const VUS = Number(__ENV.VUS || '200');
const RAMP_UP = __ENV.RAMP_UP || '30s';
const STEADY_STATE = __ENV.STEADY_STATE || '2m';
const RAMP_DOWN = __ENV.RAMP_DOWN || '30s';
const SUMMARY_MD = __ENV.SUMMARY_MD || 'performance-summary.md';
const SUMMARY_JSON = __ENV.SUMMARY_JSON || 'performance-summary.json';
const SUMMARY_HTML = __ENV.SUMMARY_HTML || 'performance-report.html';

// Threshold configuration
const MAX_ERROR_RATE = 'rate<0.01';
const MAX_P95_DURATION = 'p(95)<500';
const MAX_P99_DURATION = 'p(99)<1000';
const MIN_CHECK_RATE = 'rate>0.99';

export const options = {
  scenarios: {
    cart_api_load: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: RAMP_UP, target: VUS },
        { duration: STEADY_STATE, target: VUS },
        { duration: RAMP_DOWN, target: 0 },
      ],
      exec: 'cartApiScenario',
    },
  },
  summaryTrendStats: ['avg', 'min', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
  thresholds: {
    http_req_failed: [MAX_ERROR_RATE],
    http_req_duration: [MAX_P95_DURATION, MAX_P99_DURATION],
    'http_req_duration{endpoint:cart_add}': [MAX_P95_DURATION],
    'checks{flow:cart_api}': [MIN_CHECK_RATE],
  },
};

const commonHeaders = {
  headers: {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${AUTH_TOKEN}`,
  },
};

export function setup() {
  cleanupCartItem();
}

export function teardown() {
  cleanupCartItem();
}

export function cartApiScenario() {
  cleanupCartItem();

  const addPayload = JSON.stringify({ productId: PRODUCT_ID, quantity: 1 });
  const addRes = http.post(`${BASE_URL}/api/cart/add`, addPayload, {
    ...commonHeaders,
    tags: { endpoint: 'cart_add' },
  });
  check(
    addRes,
    {
      'POST /api/cart/add returns 200': (r) => r.status === 200,
      'POST /api/cart/add p95 target sample < 500ms': (r) => r.timings.duration < 500,
      'POST /api/cart/add response contains success=true': (r) =>
        safeJson(r)?.success === true,
    },
    { flow: 'cart_api' },
  );

  const deleteRes = cleanupCartItem();
  check(
    deleteRes,
    {
      'DELETE /api/cart/{userId}/{productId} returns 200': (r) => r.status === 200,
    },
    { flow: 'cart_api' },
  );

  sleep(THINK_TIME_SECONDS);
}

function cleanupCartItem() {
  return http.del(`${BASE_URL}/api/cart/${USER_ID}/${PRODUCT_ID}`, null, {
    ...commonHeaders,
    tags: { endpoint: 'cart_delete' },
  });
}

function safeJson(response) {
  try {
    return response.json();
  } catch {
    return null;
  }
}

export function handleSummary(data) {
  const p95 = metricValue(data, 'http_req_duration', 'p(95)');
  const p99 = metricValue(data, 'http_req_duration', 'p(99)');
  const throughput = metricValue(data, 'http_reqs', 'rate');
  const errorRate = ratePercent(data, 'http_req_failed');
  const checkRate = ratePercent(data, 'checks');
  const iterations = counterValue(data, 'iterations');
  const httpReqs = counterValue(data, 'http_reqs');
  const durationSeconds = (data.state.testRunDurationMs / 1000).toFixed(2);

  const summary = [
    '# ShopCart Performance Test Summary',
    '',
    `- API under test: Cart flow on ${BASE_URL}`,
    `- Concurrent users: ${VUS} VUs`,
    `- Duration: ramp-up ${RAMP_UP}, steady ${STEADY_STATE}, ramp-down ${RAMP_DOWN}`,
    `- Product: ${PRODUCT_ID}`,
    '',
    '## Key Metrics',
    '',
    `- Response time p95: ${p95} ms`,
    `- Response time p99: ${p99} ms`,
    `- Throughput: ${throughput} req/s`,
    `- Error rate: ${errorRate}`,
    `- Check pass rate: ${checkRate}`,
    '',
    '## Thresholds',
    '',
    '- `http_req_failed < 1%`',
    '- `http_req_duration p95 < 500ms`',
    '- `http_req_duration p99 < 1000ms`',
    '- `cart_api checks > 99%`',
    '',
  ].join('\n');

  return {
    stdout: summary,
    [SUMMARY_MD]: summary,
    [SUMMARY_JSON]: JSON.stringify(data, null, 2),
    [SUMMARY_HTML]: buildHtmlReport({
      data,
      p95,
      p99,
      throughput,
      errorRate,
      checkRate,
      iterations,
      httpReqs,
      durationSeconds,
    }),
  };
}

function metricValue(data, metricName, valueName) {
  const value = data.metrics?.[metricName]?.values?.[valueName];
  return value === undefined ? 'n/a' : Number(value).toFixed(2);
}

function counterValue(data, metricName) {
  const value = data.metrics?.[metricName]?.values?.count;
  return value === undefined ? 'n/a' : formatInteger(value);
}

function ratePercent(data, metricName) {
  const value = data.metrics?.[metricName]?.values?.rate;
  return value === undefined ? 'n/a' : `${(Number(value) * 100).toFixed(2)}%`;
}

function thresholdStatus(data, metricName, thresholdName) {
  return data.metrics?.[metricName]?.thresholds?.[thresholdName]?.ok ? 'Pass' : 'Fail';
}

function checkRows(data) {
  return (data.root_group.checks || [])
    .map(
      (item) => `
            <tr>
              <td>${escapeHtml(item.name)}</td>
              <td>${formatInteger(item.passes)}</td>
              <td>${formatInteger(item.fails)}</td>
              <td class="${item.fails === 0 ? 'ok' : 'warn'}">${item.fails === 0 ? 'Pass' : 'Co fail'}</td>
            </tr>`,
    )
    .join('');
}

function buildHtmlReport({
  data,
  p95,
  p99,
  throughput,
  errorRate,
  checkRate,
  iterations,
  httpReqs,
  durationSeconds,
}) {
  const allDuration = data.metrics.http_req_duration.values;
  const cartAddDuration = data.metrics['http_req_duration{endpoint:cart_add}'].values;
  const failedThreshold = thresholdStatus(data, 'http_req_failed', MAX_ERROR_RATE);
  const p95Threshold = thresholdStatus(data, 'http_req_duration', MAX_P95_DURATION);
  const p99Threshold = thresholdStatus(data, 'http_req_duration', MAX_P99_DURATION);
  const checksThreshold = thresholdStatus(data, 'checks{flow:cart_api}', MIN_CHECK_RATE);

  return `<!doctype html>
<html lang="vi">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>ShopCart k6 Performance Report</title>
    <style>
      :root {
        --bg: #f6f7f9;
        --panel: #ffffff;
        --text: #1f2937;
        --muted: #667085;
        --line: #d9dee7;
        --good: #157347;
        --warn: #9a6700;
        --bad: #b42318;
        --accent: #0f766e;
      }
      * { box-sizing: border-box; }
      body {
        margin: 0;
        background: var(--bg);
        color: var(--text);
        font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
      }
      main {
        width: min(1120px, calc(100vw - 32px));
        margin: 0 auto;
        padding: 32px 0 48px;
      }
      header {
        display: flex;
        justify-content: space-between;
        gap: 24px;
        align-items: flex-end;
        margin-bottom: 24px;
      }
      h1 { margin: 0 0 8px; font-size: 32px; line-height: 1.15; }
      h2 { margin: 0 0 14px; font-size: 18px; }
      p { margin: 0; color: var(--muted); }
      .badge {
        display: inline-flex;
        align-items: center;
        min-height: 32px;
        padding: 6px 12px;
        border: 1px solid #a7d8bc;
        border-radius: 6px;
        background: #eefaf2;
        color: var(--good);
        font-weight: 700;
      }
      .grid {
        display: grid;
        grid-template-columns: repeat(4, minmax(0, 1fr));
        gap: 14px;
        margin-bottom: 18px;
      }
      .panel, .metric {
        border: 1px solid var(--line);
        border-radius: 8px;
        background: var(--panel);
      }
      .metric { padding: 16px; }
      .metric .label { color: var(--muted); font-size: 13px; }
      .metric .value {
        margin-top: 8px;
        font-size: 26px;
        font-weight: 760;
        line-height: 1.1;
      }
      .metric .hint { margin-top: 8px; color: var(--muted); font-size: 12px; }
      .panel { padding: 18px; margin-top: 18px; }
      table { width: 100%; border-collapse: collapse; border-radius: 8px; overflow: hidden; }
      th, td {
        border-bottom: 1px solid var(--line);
        padding: 12px 10px;
        text-align: left;
        vertical-align: top;
      }
      th { color: var(--muted); font-size: 13px; font-weight: 700; }
      tr:last-child td { border-bottom: 0; }
      .ok { color: var(--good); font-weight: 700; }
      .warn { color: var(--warn); font-weight: 700; }
      .bad { color: var(--bad); font-weight: 700; }
      .note {
        border-left: 4px solid var(--accent);
        padding: 12px 14px;
        background: #effaf8;
        color: #134e4a;
        border-radius: 6px;
      }
      @media (max-width: 900px) {
        header { align-items: flex-start; flex-direction: column; }
        .grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
      }
      @media (max-width: 560px) {
        main { width: min(100vw - 20px, 1120px); padding-top: 20px; }
        .grid { grid-template-columns: 1fr; }
        table { font-size: 13px; }
      }
    </style>
  </head>
  <body>
    <main>
      <header>
        <div>
          <h1>ShopCart k6 Performance Report</h1>
          <p>Cart API load test: POST /api/cart/add, cleanup bang DELETE cart item</p>
        </div>
        <span class="badge">Threshold ${failedThreshold === 'Pass' && p95Threshold === 'Pass' && p99Threshold === 'Pass' && checksThreshold === 'Pass' ? 'Pass' : 'Fail'}</span>
      </header>

      <section class="grid" aria-label="Metric overview">
        <article class="metric">
          <div class="label">Concurrent Users</div>
          <div class="value">${VUS} VUs</div>
          <div class="hint">Ramp-up ${RAMP_UP}, steady ${STEADY_STATE}, ramp-down ${RAMP_DOWN}</div>
        </article>
        <article class="metric">
          <div class="label">Response Time p95</div>
          <div class="value">${p95} ms</div>
          <div class="hint">Threshold: &lt; 500 ms</div>
        </article>
        <article class="metric">
          <div class="label">Throughput</div>
          <div class="value">${throughput} req/s</div>
          <div class="hint">${httpReqs} total HTTP requests</div>
        </article>
        <article class="metric">
          <div class="label">Error Rate</div>
          <div class="value">${errorRate}</div>
          <div class="hint">Threshold: &lt; 1%</div>
        </article>
      </section>

      <section class="panel">
        <h2>Run Summary</h2>
        <table>
          <tbody>
            <tr><th>Base URL</th><td>${escapeHtml(BASE_URL)}</td></tr>
            <tr><th>Product</th><td>${escapeHtml(PRODUCT_ID)}</td></tr>
            <tr><th>Iterations</th><td>${iterations}</td></tr>
            <tr><th>Duration</th><td>${durationSeconds} seconds</td></tr>
            <tr><th>Check Pass Rate</th><td>${checkRate}</td></tr>
          </tbody>
        </table>
      </section>

      <section class="panel">
        <h2>Response Time</h2>
        <table>
          <thead>
            <tr>
              <th>Metric</th>
              <th>Average</th>
              <th>Median</th>
              <th>p90</th>
              <th>p95</th>
              <th>p99</th>
              <th>Max</th>
            </tr>
          </thead>
          <tbody>
            ${durationRow('All HTTP requests', allDuration)}
            ${durationRow('POST /api/cart/add', cartAddDuration)}
          </tbody>
        </table>
      </section>

      <section class="panel">
        <h2>Thresholds</h2>
        <table>
          <thead>
            <tr><th>Threshold</th><th>Actual</th><th>Status</th></tr>
          </thead>
          <tbody>
            ${thresholdRow('http_req_failed < 1%', errorRate, failedThreshold)}
            ${thresholdRow('http_req_duration p95 < 500 ms', `${p95} ms`, p95Threshold)}
            ${thresholdRow('http_req_duration p99 < 1000 ms', `${p99} ms`, p99Threshold)}
            ${thresholdRow('checks{flow:cart_api} > 99%', checkRate, checksThreshold)}
          </tbody>
        </table>
      </section>

      <section class="panel">
        <h2>Checks</h2>
        <table>
          <thead>
            <tr><th>Check</th><th>Passes</th><th>Fails</th><th>Status</th></tr>
          </thead>
          <tbody>${checkRows(data)}</tbody>
        </table>
      </section>

    </main>
  </body>
</html>`;
}

function durationRow(name, values) {
  return `
            <tr>
              <td>${escapeHtml(name)}</td>
              <td>${Number(values.avg).toFixed(2)} ms</td>
              <td>${Number(values.med).toFixed(2)} ms</td>
              <td>${Number(values['p(90)']).toFixed(2)} ms</td>
              <td>${Number(values['p(95)']).toFixed(2)} ms</td>
              <td>${Number(values['p(99)']).toFixed(2)} ms</td>
              <td>${Number(values.max).toFixed(2)} ms</td>
            </tr>`;
}

function thresholdRow(name, actual, status) {
  return `
            <tr>
              <td>${escapeHtml(name)}</td>
              <td>${escapeHtml(actual)}</td>
              <td class="${status === 'Pass' ? 'ok' : 'bad'}">${status}</td>
            </tr>`;
}

function escapeHtml(value) {
  return String(value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

function formatInteger(value) {
  return String(Math.round(Number(value))).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
}
