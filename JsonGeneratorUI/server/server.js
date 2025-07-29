const express = require("express");
const cors = require("cors");
//const bodyParser = require("body-parser");
const path = require("path");
const package = require("./package.json");
const createError = require("http-errors");
const logger = require("morgan");
const swaggerUi = require("swagger-ui-express");
const fs = require("fs");
const YAML = require("yaml");

// run dotenv
require("dotenv").config();

// get the JSON Library path
global.json_lib_path = process.env.JSON_LIB_PATH;

// get the Allowed Operations Per Specification Class
global.crud_map = JSON.parse(process.env.CRUD_MAP);

const app = express();
// configure app
// app.use(bodyParser.urlencoded({ extended: true }));
// app.use(bodyParser.json());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
const corsOptions = {
  origin: [`http://localhost/`],
};
app.use(cors(corsOptions));
// app.use(cors({ origin: /http:\/\/localhost/ }));
// app.options("*", cors());
app.use(express.static("public"));

// view engine setup
app.set("views", path.join(__dirname, "views"));
app.set("view engine", "pug");

/**
 * Configure 'morgan' HTTP request logger middleware.
 * 'dev': Concise output colored by response status for development use.
 */
app.use(logger("dev"));

/**
 * Assign all roots and router pairs with a
 * loop over an array of objects.
/** @type {*} */
const mapRouters = [
  // { root: "/executionspecs", router: require("./routes/executionspecs") },
  { root: "/", router: require("./routes/home") },
  { root: "/config", router: require("./routes/config") }, // JSON Library Endpoint Configuration
  { root: "/categories", router: require("./routes/categories") }, // JSON Library Specification Categories
  { root: "/executionspecs", router: require("./routes/executionspecs") },
  { root: "/hosts", router: require("./routes/hosts") },
  { root: "/datasets", router: require("./routes/datasets") },
  { root: "/querysets", router: require("./routes/querysets") },
  { root: "/reportspecs", router: require("./routes/reportspecs") },
  { root: "/reportsources", router: require("./routes/reportsources") },
  { root: "/workloads", router: require("./routes/workloads") },
];

for (rootPair of mapRouters) {
  app.use(rootPair.root, rootPair.router);
}

/**
 * Catch various errors
 */
// catch 404 and forward to error handler
//const is404 = false;
// const error_404 = (req, res, next) => {
//   // next(createError(404));
//   if (is404) {
//     res.render("myerror", { message: "Error 404 - resource not found" });
//   } else {
//     next(createError(400));
//   }
// };
// app.use(error_404);

// // error handler
// const err_hanlder = (err, res, req, next) => {
//   // set locals, only providing error in development
//   res.locals.message = err.message;
//   res.locals.error = err;

//   // render the error page
//   res.status(err.status || 500);
//   res.render("error", { error: err });
// };
// app.use(err_hanlder);

// Initialize swagger-jsdoc -> returns validated swagger spec in json format
const swaggerInitSpecification = fs.readFileSync(
  "./openapi/swaggerInit.yaml",
  "utf8"
);
const swaggerDocument = YAML.parse(swaggerInitSpecification);
app.use(
  "/api-docs",
  swaggerUi.serve,
  swaggerUi.setup(swaggerDocument, { explorer: true })
);

const port = process.env.PORT || 5000;
app.listen(port, () => {
  console.log(
    `${package.description} v${package.version} is up and running at port ${port}!`
  );
});
