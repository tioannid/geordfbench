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

// render an "Edit Report Specification" form
router.get("/edit/:spec", async (req, res) => {
  const Title = "Edit Report Specification";
  const spec = req.params.spec;
  const url = `${req.app.locals.endpointConfig.ENDPOINT_URL}/reportspecs/${spec}`;
  try {
    const { body: reportspec } = await HTTP.get(url);
    res.render("reportspecs/edit", {
      title: Title,
      spec: spec,
      data: reportspec,
      putBaseUrl: `${req.app.locals.endpointConfig.ENDPOINT_URL}/reportspecs`,
    });
  } catch (error) {
    res.status(404).render("error", {
      title: "Error",
      message: `Report specification '${spec}' not found`,
      error: error
    });
  }
});

// render a "Delete Report Specification" confirmation form
router.get("/delete/:spec", async (req, res) => {
  const Title = "Delete Report Specification";
  const spec = req.params.spec;
  const url = `${req.app.locals.endpointConfig.ENDPOINT_URL}/reportspecs/${spec}`;
  try {
    const { body: reportspec } = await HTTP.get(url);
    res.render("reportspecs/delete", {
      title: Title,
      spec: spec,
      data: reportspec,
      deleteBaseUrl: `${req.app.locals.endpointConfig.ENDPOINT_URL}/reportspecs`,
    });
  } catch (error) {
    res.status(404).render("error", {
      title: "Error",
      message: `Report specification '${spec}' not found`,
      error: error
    });
  }
});

module.exports = router;
