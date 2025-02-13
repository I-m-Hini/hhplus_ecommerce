# DB Index 최적화 보고서

---

## 1. 인덱스 개념
### 1.1 인덱스란?
인덱스(Index)는 데이터베이스에서 **검색 성능을 최적화**하기 위해 사용하는 자료구조이다.<br> 
인덱스는 테이블의 특정 컬럼에 대해 정렬된 형태로 저장되며, 데이터 조회 시 **검색 속도를 대폭 향상**시킬 수 있다.

### 1.2 인덱스의 주요 기능
- **빠른 검색 속도 제공**: `WHERE`, `ORDER BY`, `GROUP BY` 등에서 인덱스를 활용하여 성능을 최적화함
- **불필요한 테이블 전체 스캔(Tablescan) 방지**: 필요한 데이터만 빠르게 찾을 수 있도록 지원
- **데이터 정렬 최적화**: 정렬(`ORDER BY`)을 미리 적용하여 추가적인 정렬 비용을 줄임

### 1.3 인덱스의 종류
1. **Primary Key Index (기본 키 인덱스)**
    - 테이블의 기본 키(Primary Key)에 자동으로 생성되는 **유니크한 클러스터형 인덱스**
2. **Unique Index (유니크 인덱스)**
    - 중복을 허용하지 않는 컬럼에 대해 생성하는 인덱스
        <br>(`@UniqueConstraint` 사용 가능)
3. **Single Column Index (단일 컬럼 인덱스)**
   - 하나의 컬럼에 대해 생성하는 일반적인 인덱스 <br>(`CREATE INDEX idx_column ON table (column);`)
4. **Composite Index (복합 인덱스)**
    - 두 개 이상의 컬럼을 조합하여 생성하는 인덱스 <br>(`CREATE INDEX idx_composite ON table (column1, column2);`)
5. **Covering Index (커버링 인덱스)**
    - SELECT할 컬럼이 모두 포함된 인덱스로, 쿼리 실행 속도를 크게 향상
6. **Full-Text Index (전문 검색 인덱스)**
    - 텍스트 데이터에서 키워드 검색을 빠르게 수행하기 위한 인덱스

### 1.4 인덱스 사용 시 주의할 점
- **`INSERT`, `UPDATE`, `DELETE` 속도 저하**: 인덱스가 많아지면 데이터 변경 작업 시 **추가적인 인덱스 유지 비용 발생**
- **과도한 인덱스 생성 지양**: 자주 사용되지 않는 인덱스는 오히려 성능 저하를 유발할 수 있음
- **적절한 인덱스 선정이 중요**: `WHERE`, `JOIN`, `ORDER BY`, `GROUP BY`에서 **자주 사용되는 컬럼**을 중심으로 인덱스를 설계해야 함

---

## 2. 개요
이 보고서는 현재 데이터베이스에서 수행되는 주요 쿼리를 분석하고, 적절한 인덱스를 추가하여 성능 개선을 진행한 결과를 정리한 문서이다. 각 쿼리에 대한 인덱스 추가 전후의 `EXPLAIN ANALYZE` 실행 결과를 비교하여, 성능 향상 정도를 기록한다.

---

## 3. 상위 상품 목록 조회
### 코드
```java
@Override
public List<TopOrderProduct> findTop5OrderProducts() {
QOrderProduct orderProduct = QOrderProduct.orderProduct;

        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);

        return queryFactory.select(Projections.constructor(
                        TopOrderProduct.class,
                        orderProduct.productId,
                        orderProduct.quantity.sum().as("totalQuantity")
                ))
                .from(orderProduct)
                .where(orderProduct.createdAt.after(threeDaysAgo))
                .groupBy(orderProduct.productId)
                .orderBy(orderProduct.quantity.sum().desc())
                .limit(5)
                .fetch();
    }
```
### 쿼리
```sql
SELECT
    op.product_id
     , SUM(op.quantity) AS totalQuantity
FROM order_product op
WHERE op.created_at >= '2025-02-10'
GROUP BY op.product_id
ORDER BY totalQuantity DESC
LIMIT 5;
```

### 테스트 데이터 건수
: 1,632,376개

### 인덱스 적용 전
- 실행 계획
```sql
-> Limit: 5 row(s)  (actual time=11912..11912 rows=5 loops=1)
    -> Sort: totalQuantity DESC, limit input to 5 row(s) per chunk  (actual time=11911..11911 rows=5 loops=1)
        -> Table scan on <temporary>  (actual time=11847..11868 rows=70384 loops=1)
            -> Aggregate using temporary table  (actual time=11847..11847 rows=70384 loops=1)
                -> Filter: (op.created_at >= TIMESTAMP'2025-02-10 00:00:00')  (cost=167368 rows=524545) (actual time=22.5..11294 rows=347872 loops=1)
                    -> Table scan on op  (cost=167368 rows=1.57e+6) (actual time=22.5..11010 rows=1.63e+6 loops=1)
```
- 실행 시간
```sql
[2025-02-12 03:02:52] 12 s 457 ms (execution: 11 s 961 ms, fetching: 496 ms)
```

### 문제점
- `WHERE op.created_at >= '2025-02-10'` 조건으로 인해 전체 데이터를 스캔하는 경우 성능 저하 발생
- `GROUP BY product_id` 및 `ORDER BY SUM(quantity) DESC`에서 정렬 비용이 높음

### 인덱스 적용
```sql
CREATE INDEX idx_order_product_composite ON order_product (created_at, product_id, quantity);
```

### 인덱스 적용 후
- 실행 계획
```sql
-> Limit: 5 row(s)  (actual time=731..731 rows=5 loops=1)
    -> Sort: totalQuantity DESC, limit input to 5 row(s) per chunk  (actual time=731..731 rows=5 loops=1)
        -> Table scan on <temporary>  (actual time=692..704 rows=70384 loops=1)
            -> Aggregate using temporary table  (actual time=692..692 rows=70384 loops=1)
                -> Filter: (op.created_at >= TIMESTAMP'2025-02-10 00:00:00')  (cost=132660 rows=649476) (actual time=1.89..405 rows=347872 loops=1)
                    -> Covering index range scan on op using idx_order_product_composite over ('2025-02-10 00:00:00.000000' <= created_at)  (cost=132660 rows=649476) (actual time=1.82..338 rows=347872 loops=1)
```
- 실행 시간
```sql
[2025-02-12 03:03:50] 909 ms (execution: 750 ms, fetching: 159 ms)
```

### 성능 비교
|         적용 전          |             적용 후             |              
|:---------------------:|:------------------------------:|
|       11.9초 실행        |            0.75초 실행            |
| `Table scan on op` 발생 | `Covering index range scan` 적용 |

### 개선 효과
- `created_at`을 인덱스로 활용하여 범위 스캔 수행
-  `product_id`를 기반으로 `GROUP BY` 최적화
-  `quantity`가 인덱스에 포함되어 있어 `SUM(quantity)` 연산 속도 개선

---

## 4. 상품 재고 조회
### 코드
```java
Stock findByProductId(Long productId);
```
### 쿼리
```sql
SELECT quantity
FROM stock
WHERE product_id = 195321;
```
### 테스트 데이터 건수
: 160만개

### 인덱스 적용 전
- 실행 계획
```sql
-> Filter: (stock.product_id = 195321)  (cost=163562 rows=158852) (actual time=6666..6666 rows=0 loops=1)
    -> Table scan on stock  (cost=163562 rows=1.59e+6) (actual time=13.4..6481 rows=1.6e+6 loops=1)
```
- 실행 시간
```sql
[2025-02-12 03:05:16] 6 s 875 ms (execution: 6 s 679 ms, fetching: 196 ms)
```

### 문제점
- `product_id`에 대한 인덱스가 없어 `Table scan` 발생

### 인덱스 적용
```sql
CREATE INDEX idx_stock_product_id ON stock (product_id);
```

### 인덱스 적용 후
- 실행 계획
```sql
-> Index lookup on stock using idx_stock_product_id (product_id=195321)  (cost=0.995 rows=1) (actual time=0.0225..0.0225 rows=0 loops=1)
```
- 실행 시간
```sql
[2025-02-12 03:06:01] 251 ms (execution: 7 ms, fetching: 244 ms)
```

### 성능 비교
|           적용 전           |             적용 후             |              
|:------------------------:|:------------------------------:|
|         6.87초 실행         |            0.25초 실행            |
| `Table scan on stock` 발생 | `Index lookup` 적용 |

### 개선 효과
- `product_id` 인덱스를 활용하여 빠른 조회 가능
- `Table scan` 대신 `Index lookup` 사용으로 성능 대폭 개선

---

## 5. 사용자 포인트 조회
### 코드
```java
UserPoint findByUserId(Long userId);
```
### 쿼리
```sql
SELECT amount
FROM user_point
WHERE user_id = 954231;
```
### 테스트 데이터 건수
: 160만개

### 인덱스 적용 전
- 실행 계획
```sql
-> Filter: (user_point.user_id = 954231)  (cost=161123 rows=159563) (actual time=566..838 rows=1 loops=1)
    -> Table scan on user_point  (cost=161123 rows=1.6e+6) (actual time=8.73..741 rows=1.6e+6 loops=1)
```
- 실행 시간
```sql
[2025-02-12 02:53:24] 1 s 270 ms (execution: 883 ms, fetching: 387 ms)
```

### 문제점
- `user_id`에 대한 인덱스가 없어 `Table scan` 발생

### 인덱스 적용
```sql
CREATE INDEX idx_user_point_user_id ON user_point (user_id);
```

### 인덱스 적용 후
- 실행 계획
```sql
-> Index lookup on user_point using idx_user_point_user_id (user_id=954231)  (cost=0.396 rows=1) (actual time=0.755..0.763 rows=1 loops=1)
```
- 실행 시간
```sql
[2025-02-12 02:55:04] 881 ms (execution: 79 ms, fetching: 802 ms)
```

### 성능 비교
|           적용 전           |       적용 후        |              
|:------------------------:|:-----------------:|
|         1.27초 실행         |      0.08초 실행      |
| `Table scan` 발생 | `Index lookup` 적용 |

### 개선 효과
- `user_id` 인덱스를 활용하여 빠른 조회 가능
- `Table scan` 대신 `Index lookup` 사용으로 성능 대폭 개선

---

## 6. 사용자 쿠폰 발급 목록 조회
### 코드
```java
List<CouponIssuance> findByUserId(Long userId);
```
### 쿼리
```sql
SELECT issuance_id
     , coupon_id
     , coupon_state
FROM coupon_issuance
WHERE user_id = 195325
```
### 테스트 데이터 건수
: 80만개

### 분석
- `user_id`와 `coupon_id`의 조합에 대해 **유니크 제약 조건**이 설정되어 있음
- 사용자가 **같은 쿠폰을 중복 발급받을 수 없도록** `@UniqueConstraint`를 적용함
- 이로 인해 자동으로 **유니크 인덱스(Unique Index)**가 생성됨, 해당 인덱스가 쿼리 실행 시 활용됨

### 인덱스 적용
```java
@Table(name="CouponIssuance", uniqueConstraints = {
    @UniqueConstraint(
        name="user_coupon_uk",
        columnNames={"userId","couponId"}
    )})
```

### 인덱스 적용 후
- 실행 계획
```sql
-> Index lookup on coupon_issuance using user_coupon_uk (user_id=195325)  (cost=0.35 rows=1) (actual time=0.0118..0.0118 rows=0 loops=1)
```
- 실행 시간
```sql
[2025-02-12 03:12:57] 337 ms (execution: 11 ms, fetching: 326 ms)
```

### 개선 효과
- `user_id` + `coupon_id` 복합 유니크 인덱스를 활용하여 조회 속도 개선
- `Index lookup`을 적용하여 빠른 검색 가능
- **사용자가 동일한 쿠폰을 중복 발급받을 수 없도록** 하는 비즈니스 로직을 DB 레벨에서 보장

---

## 7. 결론
본 보고서에서는 **자주 실행되는 주요 쿼리**에 대해 인덱스를 적용하여 **성능 개선 효과를 측정**하였다.

그 결과, 인덱스 추가 전후의 성능 차이가 크며, **적절한 인덱스 추가가 쿼리 최적화에 필수적임**을 확인할 수 있었다.

### 인덱스 성능 요약
|쿼리|인덱스 전 실행시간|인덱스 후 실행시간|성능 개선|
|:----|:---:|:---:|:----|
|상위 상품 목록 조회|11.9초|0.75초|15배 이상 개선|
|상품 재고 조회|6.87초|0.25초|27배 이상 개선|
|사용자 포인트 조회|1.27초|0.08초|15배 이상 개선|

### 결론
적절한 인덱스를 추가하면 쿼리 성능이 획기적으로 개선됨을 확인하였다.<br>
특히 자주 조회되는 데이터에 대해 `WHERE` 조건과 `GROUP BY` 최적화를 위한 **복합 인덱스**를 활용하면 **불필요한 Table Scan을 제거하여 성능을 극대화할 수 있다.**