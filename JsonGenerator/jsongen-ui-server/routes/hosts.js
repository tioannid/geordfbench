var express = require("express");
var router = express.Router();
const { HTTP } = require("http-call");

// get a list of hosts for use with a PUG template
router.get("/", async (req, res) => {
  const Title = "List of Host Specifications";
  const url = `${req.app.locals.endpointConfig.ENDPOINT_URL}/hosts`;
  const { body: hosts } = await HTTP.get(url);
  return res.render("hosts/list", {
    title: Title,
    records: hosts,
  });
});

// render a "New Host Specification" form
router.get("/new", (req, res) => {
  const Title = "Create New Host Specification";
  res.render("hosts/new", {
    title: Title,
    postBaseUrl: `${req.app.locals.endpointConfig.ENDPOINT_URL}/hosts`,
  });
});

// render an "Edit Host Specification" form
router.get("/edit/:spec", async (req, res) => {
  const Title = "Edit Host Specification";
  const spec = req.params.spec;
  const url = `${req.app.locals.endpointConfig.ENDPOINT_URL}/hosts/${spec}`;
  try {
    const { body: host } = await HTTP.get(url);
    res.render("hosts/edit", {
      title: Title,
      spec: spec,
      data: host,
      putBaseUrl: `${req.app.locals.endpointConfig.ENDPOINT_URL}/hosts`,
    });
  } catch (error) {
    res.status(404).render("error", {
      title: "Error",
      message: `Host specification '${spec}' not found`,
      error: error
    });
  }
});

// render a "Delete Host Specification" confirmation form
router.get("/delete/:spec", async (req, res) => {
  const Title = "Delete Host Specification";
  const spec = req.params.spec;
  const url = `${req.app.locals.endpointConfig.ENDPOINT_URL}/hosts/${spec}`;
  try {
    const { body: host } = await HTTP.get(url);
    res.render("hosts/delete", {
      title: Title,
      spec: spec,
      data: host,
      deleteBaseUrl: `${req.app.locals.endpointConfig.ENDPOINT_URL}/hosts`,
    });
  } catch (error) {
    res.status(404).render("error", {
      title: "Error",
      message: `Host specification '${spec}' not found`,
      error: error
    });
  }
});

module.exports = router;
