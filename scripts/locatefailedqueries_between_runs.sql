-- LOCATE FAILED QUERIES BETWEEN RUNS (either for the same or different systems)
--
-- Find the differences in the number of results returned between 2 system runs
-- PLEASE modify the experiment_id values in the subqueries
SELECT g.*, r.eval_flag AS r_eval_flag, r.res_exception AS r_res_exception, r.no_results AS r_no_results, ABS(g.no_results-r.no_results) AS results_diff
FROM 
(SELECT * FROM public."QUERYEXECUTION" WHERE experiment_id=544) g, -- GraphDB results
(SELECT * FROM public."QUERYEXECUTION" WHERE experiment_id=545) r -- RDF4J results
WHERE (g.query_no=r.query_no) AND (g.cache_type=r.cache_type) AND (g.iteration=r.iteration) AND
      ((g.eval_flag<>r.eval_flag) OR (g.res_exception<>r.res_exception))
ORDER BY g.cache_type, results_diff DESC