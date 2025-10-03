const { Pool } = require("pg");
const path = require("path");
require("dotenv").config();
const { spawn } = require("child_process"); // spawn child processes both in ansynchronous as asynchronous manner

// get the H2 database properties and start H2 database
const H2_BASEDIR = process.env.H2_BASEDIR;
const H2_JAR = process.env.H2_JAR;
const h2JarPath = path.join(__dirname, H2_JAR);
const H2_HOST = process.env.H2_HOST;
const H2_PORT = process.env.H2_PORT;
const H2_DBNAME = process.env.H2_DBNAME;
const h2DBPath = path.join(__dirname, H2_DBNAME);
const H2_USER = process.env.H2_USER;
const H2_PWD = process.env.H2_PWD;
const H2_WEB_PORT = process.env.H2_WEB_PORT;
const H2_CMD = `java -Dh2.bindAddress=${H2_HOST} -cp ${h2JarPath} org.h2.tools.Server -ifNotExists -pg -pgPort ${H2_PORT} -web -webPort ${H2_WEB_PORT} -baseDir ${__dirname}`;
const H2_JDBC_CONN = `jdbc:h2:tcp://localhost/${h2DBPath};MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;`;
const ENDPOINT_DB_SCHEMA_SCRIPT = `
CREATE SCHEMA IF NOT EXISTS ENDPOINT AUTHORIZATION SA;
SET SCHEMA ENDPOINT;
CREATE TABLE IF NOT EXISTS "USER" (
	id int GENERATED ALWAYS AS IDENTITY PRIMARY KEY, 
	instime timestamp(3) with time zone DEFAULT CURRENT_TIMESTAMP(3),
	name character varying(30) UNIQUE,
	passwd character varying(255),
	salt character varying(255),
	description character varying(255)
	);
`;

// Run the H2 database server
async function runH2Server() {
  const child = await spawn(H2_CMD, {
    stdio: "ignore",
    shell: true,
    cwd: `${__dirname}`,
    detached: true,
  });

  // create a delay
  let n = 0;
  for (i = 0; i <= 1000000000; i++) {
    n++;
  }

  // initialize schema if needed
  var pool = await getPool();
  try {
    let result = await pool.query(ENDPOINT_DB_SCHEMA_SCRIPT);
    return child;
  } catch (err) {
    console.log(err);
    return null;
  }
}

const h2Server = runH2Server();

// H2: PostgreSQL Compatibility Mode
// use the database URL jdbc:h2:~/test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH.
// Do not change value of DATABASE_TO_LOWER after creation of database.
const getConnStr = (client) => {
  return `${H2_JDBC_CONN}`;
};

async function funcGetPool() {
  // // Connection - CHOICE 1
  // const pool = new Pool({
  //   host: `${H2_HOST}`,
  //   port: `${H2_PORT}`,
  //   user: `${H2_USER}`,
  //   password: "",
  //   database: `${h2DBPath}`,
  //   ssl: false,
  // });
  // // console.log(pool.options);

  // Connection - CHOICE 2
  // const url = `jdbc:h2:${h2DBPath};MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH`;
  const postgresqlUrl = `postgresql://${H2_USER}:${H2_PWD}@${H2_HOST}:${H2_PORT}/${H2_DBNAME}`;
  const pool2 = new Pool({
    connectionString: postgresqlUrl,
  });
  // console.log(pool2.options);
  return pool2;
}
const getPool = funcGetPool;

module.exports = { getConnStr, getPool };
