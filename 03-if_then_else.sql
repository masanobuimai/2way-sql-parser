SELECT * FROM BOOK
/*BEGIN*/
  WHERE
  /*IF minPrice != null*/
        PRICE >= /*minPrice*/1000
  /*END*/
  /*IF maxPrice != null*/
    AND PRICE <= /*maxPrice*/2000
  /*END*/
/*END*/
