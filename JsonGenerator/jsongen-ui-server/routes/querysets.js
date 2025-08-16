var express = require("express");
var router = express.Router();
const { HTTP } = require("http-call");

router.get("/", async (req, res) => {
  const Title = "List of Query Set Specifications";
  const url = `${req.app.locals.endpointConfig.ENDPOINT_URL}/querysets`;
  const { body: querysets } = await HTTP.get(url);
  return res.render("querysets/list", { title: Title, records: querysets });
});

router.get("/new", (req, res) => {
  const Title = "Create New Query Set Specification";
  res.render("querysets/new", {
    title: Title,
    postBaseUrl: `${req.app.locals.endpointConfig.ENDPOINT_URL}/querysets`,
  });
});

router.get("/edit/:spec", async (req, res) => {
  const Title = "Edit Query Set Specification";
  const spec = req.params.spec;
  const url = `${req.app.locals.endpointConfig.ENDPOINT_URL}/querysets/${spec}`;
  try {
    const { body: queryset } = await HTTP.get(url);
    res.render("querysets/edit", {
      title: Title, spec: spec, data: queryset,
      putBaseUrl: `${req.app.locals.endpointConfig.ENDPOINT_URL}/querysets`,
    });
  } catch (error) {
    res.status(404).render("error", {
      title: "Error", message: `Query set specification '${spec}' not found`, error: error
    });
  }
});

router.get("/delete/:spec", async (req, res) => {
  const Title = "Delete Query Set Specification";
  const spec = req.params.spec;
  const url = `${req.app.locals.endpointConfig.ENDPOINT_URL}/querysets/${spec}`;
  try {
    const { body: queryset } = await HTTP.get(url);
    res.render("querysets/delete", {
      title: Title, spec: spec, data: queryset,
      deleteBaseUrl: `${req.app.locals.endpointConfig.ENDPOINT_URL}/querysets`,
    });
  } catch (error) {
    res.status(404).render("error", {
      title: "Error", message: `Query set specification '${spec}' not found`, error: error
    });
  }
});

module.exports = router;
