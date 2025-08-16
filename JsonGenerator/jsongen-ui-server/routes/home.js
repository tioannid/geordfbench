var express = require("express");
var router = express.Router();
const packageJson = require("../package.json");
const { HTTP } = require("http-call");

/* GET home page. */
router.get("/", async function (req, res, next) {
  try {
    // Get counts for each specification category
    const categories = ['datasets', 'executionspecs', 'hosts', 'querysets', 'reportsources', 'reportspecs', 'workloads'];
    const counts = {};
    
    for (const category of categories) {
      try {
        const url = `${req.app.locals.endpointConfig.ENDPOINT_URL}/${category}`;
        const { body: records } = await HTTP.get(url);
        counts[category] = Array.isArray(records) ? records.length : 0;
      } catch (error) {
        counts[category] = 0;
      }
    }
    
    res.render("home", {
      title: "GeoRDFBench Framework - JSON Specification Manager",
      version: packageJson.version,
      description: packageJson.description,
      counts: counts,
      endpointUrl: req.app.locals.endpointConfig.ENDPOINT_URL
    });
  } catch (error) {
    res.render("home", {
      title: "GeoRDFBench Framework - JSON Specification Manager",
      version: packageJson.version,
      description: packageJson.description,
      counts: {},
      endpointUrl: req.app.locals.endpointConfig.ENDPOINT_URL,
      error: error.message
    });
  }
});

module.exports = router;
