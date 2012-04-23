SELECT * FROM BOOK
  WHERE
  /*%if minPrice != null*/
        PRICE >= /* minPrice */1000
  /*%end*/
  /*%if maxPrice != null*/
    AND PRICE <= /* maxPrice */2000
  /*%end*/
