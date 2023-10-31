-- RESULTS VALIDATION BETWEEN DIFFERENT RUNS (either from different or the same system)
--
-- Find the differences in the number of results returned between 2 system runs
-- PLEASE modify the experiment_id values in the subqueries
SELECT (g.eval_flag=r.eval_flag) AS eval_flag_diff, (g.res_exception=r.res_exception) AS res_exception_diff, ABS(g.no_results-r.no_results) AS results_diff, g.*, r.*
FROM 
(SELECT * FROM public."QUERYEXECUTION" WHERE experiment_id=411) g, -- GraphDB results
(SELECT * FROM public."QUERYEXECUTION" WHERE experiment_id=409) r -- RDF4J results
WHERE (g.query_no=r.query_no) AND (g.cache_type=r.cache_type) AND (g.iteration=r.iteration) 
--	AND (g.eval_flag=r.eval_flag) AND (g.res_exception=r.res_exception)
ORDER BY eval_flag_diff DESC, res_exception_diff DESC, g.cache_type, results_diff DESC