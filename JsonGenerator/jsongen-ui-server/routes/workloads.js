var express = require("express");
var router = express.Router();
const { HTTP } = require("http-call");

router.get("/", async (req, res) => {
  const Title = "List of Workload Specifications";
  const url = `${req.app.locals.endpointConfig.ENDPOINT_URL}/workloads`;
  const { body: workloads } = await HTTP.get(url);
  return res.render("workloads/list", { title: Title, records: workloads });
});

router.get("/new", (req, res) => {
  const Title = "Create New Workload Specification";
  res.render("workloads/new", {
    title: Title,
    postBaseUrl: `${req.app.locals.endpointConfig.ENDPOINT_URL}/workloads`,
  });
});

router.get("/edit/:spec", async (req, res) => {
  const Title = "Edit Workload Specification";
  const spec = req.params.spec;
  const url = `${req.app.locals.endpointConfig.ENDPOINT_URL}/workloads/${spec}`;
  try {
    const { body: workload } = await HTTP.get(url);
    res.render("workloads/edit", {
      title: Title, spec: spec, data: workload,
      putBaseUrl: `${req.app.locals.endpointConfig.ENDPOINT_URL}/workloads`,
    });
  } catch (error) {
    res.status(404).render("error", {
      title: "Error", message: `Workload specification '${spec}' not found`, error: error
    });
  }
});

router.get("/delete/:spec", async (req, res) => {
  const Title = "Delete Workload Specification";
  const spec = req.params.spec;
  const url = `${req.app.locals.endpointConfig.ENDPOINT_URL}/workloads/${spec}`;
  try {
    const { body: workload } = await HTTP.get(url);
    res.render("workloads/delete", {
      title: Title, spec: spec, data: workload,
      deleteBaseUrl: `${req.app.locals.endpointConfig.ENDPOINT_URL}/workloads`,
    });
  } catch (error) {
    res.status(404).render("error", {
      title: "Error", message: `Workload specification '${spec}' not found`, error: error
    });
  }
});

module.exports = router;
