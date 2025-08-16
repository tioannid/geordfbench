var express = require("express");
var router = express.Router();
const { HTTP } = require("http-call");

router.get("/", async (req, res) => {
  const Title = "List of Report Source Specifications";
  const url = `${req.app.locals.endpointConfig.ENDPOINT_URL}/reportsources`;
  const { body: reportsources } = await HTTP.get(url);
  return res.render("reportsources/list", { title: Title, records: reportsources });
});

router.get("/new", (req, res) => {
  const Title = "Create New Report Source Specification";
  res.render("reportsources/new", {
    title: Title,
    postBaseUrl: `${req.app.locals.endpointConfig.ENDPOINT_URL}/reportsources`,
  });
});

router.get("/edit/:spec", async (req, res) => {
  const Title = "Edit Report Source Specification";
  const spec = req.params.spec;
  const url = `${req.app.locals.endpointConfig.ENDPOINT_URL}/reportsources/${spec}`;
  try {
    const { body: reportsource } = await HTTP.get(url);
    res.render("reportsources/edit", {
      title: Title, spec: spec, data: reportsource,
      putBaseUrl: `${req.app.locals.endpointConfig.ENDPOINT_URL}/reportsources`,
    });
  } catch (error) {
    res.status(404).render("error", {
      title: "Error", message: `Report source specification '${spec}' not found`, error: error
    });
  }
});

router.get("/delete/:spec", async (req, res) => {
  const Title = "Delete Report Source Specification";
  const spec = req.params.spec;
  const url = `${req.app.locals.endpointConfig.ENDPOINT_URL}/reportsources/${spec}`;
  try {
    const { body: reportsource } = await HTTP.get(url);
    res.render("reportsources/delete", {
      title: Title, spec: spec, data: reportsource,
      deleteBaseUrl: `${req.app.locals.endpointConfig.ENDPOINT_URL}/reportsources`,
    });
  } catch (error) {
    res.status(404).render("error", {
      title: "Error", message: `Report source specification '${spec}' not found`, error: error
    });
  }
});

module.exports = router;
