var express = require("express");
var router = express.Router();
const { HTTP } = require("http-call");

// get a list of report specifications for use with a
// PUG template
router.get("/", async (req, res) => {
  const Title = "List of Report Specifications";
  const url = `${req.app.locals.endpointConfig.ENDPOINT_URL}/reportspecs`;
  const { body: reportspecs } = await HTTP.get(url);
  return res.render("reportspecs/list", {
    title: Title,
    records: reportspecs,
  });
});

// render a "New Report Specification" form
router.get("/new", (req, res) => {
  const Title = "Create New Report Specification";
  res.render("reportspecs/new", {
    title: Title,
    postBaseUrl: `${req.app.locals.endpointConfig.ENDPOINT_URL}/reportspecs`,
  });
});

module.exports = router;
