# Load Tests

- [Load Tests](#load-tests)
  - [How to Run](#how-to-run)
    - [Requirements](#requirements)
    - [Run against the Monolith](#run-against-the-monolith)
    - [Run against Microservices](#run-against-microservices)
    - [Environment Variables](#environment-variables)
  - [Results](#results)
    - [Monolith](#monolith)
      - [All](#all)
      - [Book](#book)
    - [Microservices](#microservices)
      - [All](#all-1)
      - [Book](#book-1)
    - [Comparison](#comparison)
      - [Monolith](#monolith-1)
        - [All Endpoints](#all-endpoints)
        - [Book Module Only](#book-module-only)
      - [Microservices](#microservices-1)
        - [All Endpoints](#all-endpoints-1)
        - [Book Module Only](#book-module-only-1)
      - [Conclusions](#conclusions)

## How to Run

### Requirements

```bash
brew install k6
```

Or check <https://k6.io/docs/get-started/installation/>

### Run against the Monolith

```sh
k6 run load-test.js \
 -e ARCH=monolith \
 -e BASE_URL=http://localhost:8080/api \
 -e MODULES=books,authors
```

- All requests go to BASE_URL
- Auth is executed once in setup()

### Run against Microservices

```sh
k6 run load-test.js \
 -e ARCH=micro \
 -e MODULES=books \
 -e BOOKS_URL=http://localhost:8088/api \
 -e AUTH_URL=http://localhost:8084/api
```

- Only the modules listed in MODULES are tested.

### Environment Variables

| Variable   | Description                                                                |
| ---------- | -------------------------------------------------------------------------- |
| `ARCH`     | `monolith` or `micro`                                                      |
| `MODULES`  | Modules to test separated by commas (e.g. `MODULES=books,genres,lendings`) |
| `BASE_URL` | Monolith base URL                                                          |
| `*_URL`    | Microservice base URLs                                                     |

## Results

### Monolith

#### All

| Metric          | Value            |
| --------------- | ---------------- |
| Total requests  | **38 429**       |
| Request rate    | **124.01 req/s** |
| Failed requests | **0 (0.00%)**    |

| Stat    | Value       |
| ------- | ----------- |
| Average | **6.87 s**  |
| Median  | **8.41 s**  |
| Min     | **650 µs**  |
| Max     | **11.98 s** |
| p90     | **9.33 s**  |
| p95     | **9.57 s**  |

#### Book

| Metric          | Value            |
| --------------- | ---------------- |
| Total requests  | **190 392**      |
| Request rate    | **633.65 req/s** |
| Failed requests | **0 (0.00%)**    |

| Stat    | Value         |
| ------- | ------------- |
| Average | **1.44 ms**   |
| Median  | **976 µs**    |
| Min     | **531 µs**    |
| Max     | **285.46 ms** |
| p90     | **2.22 ms**   |
| p95     | **3.73 ms**   |

### Microservices

#### All

| Metric          | Value            |
| --------------- | ---------------- |
| Total requests  | **75 658**       |
| Request rate    | **237.04 req/s** |
| Failed requests | **0 (0.00%)**    |

| Stat    | Value       |
| ------- | ----------- |
| Average | **3.07 s**  |
| Median  | **3.94 ms** |
| Min     | **460 µs**  |
| Max     | **34.12 s** |
| p90     | **17.33 s** |
| p95     | **20.56 s** |

#### Book

| Metric          | Value            |
| --------------- | ---------------- |
| Total requests  | **190 422**      |
| Request rate    | **634.20 req/s** |
| Failed requests | **0 (0.00%)**    |

| Stat    | Value         |
| ------- | ------------- |
| Average | **869.39 µs** |
| Median  | **729 µs**    |
| Min     | **458 µs**    |
| Max     | **87.53 ms**  |
| p90     | **1.24 ms**   |
| p95     | **1.65 ms**   |

### Comparison

#### Monolith

##### All Endpoints

The monolith handled 38,429 total requests at a rate of **124 req/s** with 0 failed requests, which indicates it successfully processed all requests.

Request duration metrics are extremely high:

- Average latency: **6.87 s**
- Median: **8.41 s**
- 95th percentile: **9.57 s**

These high latencies suggest that the monolith struggles under load when all endpoints are exercised simultaneously, likely due to resource contention across modules.

##### Book Module Only

The book endpoints performed extremely well in isolation:

- Total requests: **190,392**
- Request rate: **~634 req/s**
- Average latency: **1.44 ms**
- 95th percentile: **3.73 ms**

This indicates that when isolated, the monolith can handle high throughput with very low latency for a single module, but performance degrades significantly when all modules are exercised together.

#### Microservices

##### All Endpoints

The microservices architecture processed 75,658 requests at **237 req/s**, with 0 failures.

Latency metrics show:

- Average: **3.07 s**
- Median: **3.94 ms**
- 95th percentile: **20.56 s**

While median latency is very low, the high maximum and p95 values indicate that some requests experience long tail delays, likely due to inter-service communication overhead or certain microservices being overloaded.

Compared to the monolith, the microservices setup achieves higher throughput, but latency variability is more pronounced.

##### Book Module Only

Book endpoints in microservices show excellent performance, similar to the monolith:

- Total requests: **190,422**
- Request rate: **~634 req/s**
- Average latency: **869 µs**
- 95th percentile: **1.65 ms**

This demonstrates that isolating modules as independent services can improve performance predictability, as each service can scale independently and avoid contention.

#### Conclusions

1. **Isolation Improves Module Performance:**

- Both architectures handle book endpoints efficiently, but when all endpoints are exercised, the monolith suffers from high latency, while microservices show variability but maintain higher throughput.

2. **Microservices Trade-offs:**

- Pros: Higher throughput, independent scaling, median latency remains low.
- Cons: Longer tail latencies under full load, likely due to network communication between services.

3. **Monolith Trade-offs:**

- Pros: Consistent behavior for isolated modules, minimal network overhead.
- Cons: Resource contention across modules causes extremely high latencies when all endpoints are exercised simultaneously.

4. **Further Optimization Needed:**

- Both architectures could benefit from optimizations. The monolith may need better resource management, while microservices could improve inter-service communication, load balancing and fault tolerance to reduce tail latencies. This could be improved by adding replicas to overloaded services.
