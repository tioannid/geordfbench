var express = require("express");
var router = express.Router();
const { HTTP } = require("http-call");

// get a list of specification categories for use with a
// PUG template
router.get("/", async (req, res) => {
  const Title = "List of Specification Categories";
  const url = `${req.app.locals.endpointConfig.ENDPOINT_URL}/categories`;
  const { body: categories } = await HTTP.get(url);
  return res.render("categories", {
    title: Title,
    records: categories,
  });
});

module.exports = router;
