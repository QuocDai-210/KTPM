# ShopCart Performance Test Summary

- API under test: Cart flow on http://localhost:8080
- Concurrent users: 200 VUs
- Duration: ramp-up 30s, steady 2m, ramp-down 30s
- Product: P010

## Key Metrics

- Response time p95: 1.27 ms
- Response time p99: 1.56 ms
- Throughput: 488.17 req/s
- Error rate: 0.20%
- Check pass rate: 99.78%

## Thresholds

- `http_req_failed < 1%`
- `http_req_duration p95 < 500ms`
- `http_req_duration p99 < 1000ms`
- `cart_api checks > 99%`
