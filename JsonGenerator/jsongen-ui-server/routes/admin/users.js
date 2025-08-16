var express = require("express");
var router = express.Router();
const bcrypt = require("bcrypt");
const jwt = require("jsonwebtoken");
const { getPool } = require("../../db/pg_database");

// get a list of user names only for use with a
// PUG template
router.get("/", async (req, res) => {
  const Title = "User List";
  var pool = await getPool();
  const poolResult = await pool.query('SELECT * FROM ENDPOINT."USER"');

  // an array with fields that need to be hidden
  const hideFields = ["instime", "passwd", "salt"];
  const showFieldsMap = new Map();
  poolResult.fields.forEach((field, index) => {
    if (!hideFields.includes(field.name)) {
      showFieldsMap.set(index, field.name);
    }
  });

  res.render("users/list", {
    title: Title,
    fields: poolResult.fields,
    showfields: showFieldsMap,
    records: poolResult.rows,
  });
});

// render a "New User" form
router.get("/new", (req, res) => {
  const Title = "Create New User";
  res.render("users/new", {
    title: Title,
    name: "Test name",
    password: "XXXXX",
    description: "blha blha...",
  });
});

module.exports = router;
