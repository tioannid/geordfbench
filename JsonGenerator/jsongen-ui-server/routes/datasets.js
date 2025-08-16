var express = require("express");
var router = express.Router();
const { HTTP } = require("http-call");

// get a list of datasets for use with a PUG template
router.get("/", async (req, res) => {
  const Title = "List of Datasets";
  const url = `${req.app.locals.endpointConfig.ENDPOINT_URL}/datasets`;
  const { body: datasets } = await HTTP.get(url);
  return res.render("datasets/list", {
    title: Title,
    records: datasets,
  });
});

// render a "New Dataset" form
router.get("/new", (req, res) => {
  const Title = "Create New Dataset";
  res.render("datasets/new", {
    title: Title,
    postBaseUrl: `${req.app.locals.endpointConfig.ENDPOINT_URL}/datasets`,
  });
});

// render an "Edit Dataset" form
router.get("/edit/:spec", async (req, res) => {
  const Title = "Edit Dataset";
  const spec = req.params.spec;
  const url = `${req.app.locals.endpointConfig.ENDPOINT_URL}/datasets/${spec}`;
  try {
    const { body: dataset } = await HTTP.get(url);
    res.render("datasets/edit", {
      title: Title,
      spec: spec,
      data: dataset,
      putBaseUrl: `${req.app.locals.endpointConfig.ENDPOINT_URL}/datasets`,
    });
  } catch (error) {
    res.status(404).render("error", {
      title: "Error",
      message: `Dataset '${spec}' not found`,
      error: error
    });
  }
});

// render a "Delete Dataset" confirmation form
router.get("/delete/:spec", async (req, res) => {
  const Title = "Delete Dataset";
  const spec = req.params.spec;
  const url = `${req.app.locals.endpointConfig.ENDPOINT_URL}/datasets/${spec}`;
  try {
    const { body: dataset } = await HTTP.get(url);
    res.render("datasets/delete", {
      title: Title,
      spec: spec,
      data: dataset,
      deleteBaseUrl: `${req.app.locals.endpointConfig.ENDPOINT_URL}/datasets`,
    });
  } catch (error) {
    res.status(404).render("error", {
      title: "Error",
      message: `Dataset '${spec}' not found`,
      error: error
    });
  }
});

module.exports = router;
