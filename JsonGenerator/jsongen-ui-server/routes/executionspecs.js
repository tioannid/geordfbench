var express = require("express");
var router = express.Router();
const { HTTP } = require("http-call");

// get a list of execution specs for use with a PUG template
router.get("/", async (req, res) => {
  const Title = "List of Execution Specifications";
  const url = `${req.app.locals.endpointConfig.ENDPOINT_URL}/executionspecs`;
  const { body: executionspecs } = await HTTP.get(url);
  return res.render("executionspecs/list", {
    title: Title,
    records: executionspecs,
  });
});

// render a "New Execution Specification" form
router.get("/new", (req, res) => {
  const Title = "Create New Execution Specification";
  res.render("executionspecs/new", {
    title: Title,
    postBaseUrl: `${req.app.locals.endpointConfig.ENDPOINT_URL}/executionspecs`,
  });
});

// render an "Edit Execution Specification" form
router.get("/edit/:spec", async (req, res) => {
  const Title = "Edit Execution Specification";
  const spec = req.params.spec;
  const url = `${req.app.locals.endpointConfig.ENDPOINT_URL}/executionspecs/${spec}`;
  try {
    const { body: executionspec } = await HTTP.get(url);
    res.render("executionspecs/edit", {
      title: Title,
      spec: spec,
      data: executionspec,
      putBaseUrl: `${req.app.locals.endpointConfig.ENDPOINT_URL}/executionspecs`,
    });
  } catch (error) {
    res.status(404).render("error", {
      title: "Error",
      message: `Execution specification '${spec}' not found`,
      error: error
    });
  }
});

// render a "Delete Execution Specification" confirmation form
router.get("/delete/:spec", async (req, res) => {
  const Title = "Delete Execution Specification";
  const spec = req.params.spec;
  const url = `${req.app.locals.endpointConfig.ENDPOINT_URL}/executionspecs/${spec}`;
  try {
    const { body: executionspec } = await HTTP.get(url);
    res.render("executionspecs/delete", {
      title: Title,
      spec: spec,
      data: executionspec,
      deleteBaseUrl: `${req.app.locals.endpointConfig.ENDPOINT_URL}/executionspecs`,
    });
  } catch (error) {
    res.status(404).render("error", {
      title: "Error",
      message: `Execution specification '${spec}' not found`,
      error: error
    });
  }
});

module.exports = router;
