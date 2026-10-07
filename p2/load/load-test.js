import { check, sleep } from "k6";
import http from "k6/http";

const ARCH = __ENV.ARCH || "monolith";

const MONOLITH_BASE_URL = __ENV.BASE_URL || "http://localhost:8080";

const MICRO_BASE_URLS = {
  auth: __ENV.AUTH_URL || "http://localhost:8084/api",
  authors: __ENV.BOOKS_URL || "http://localhost:8087/api",
  books: __ENV.BOOKS_URL || "http://localhost:8087/api",
  genres: __ENV.GENRES_URL || "http://localhost:8088/api",
  readers: __ENV.READERS_URL || "http://localhost:8090/api",
  lendings: __ENV.LENDINGS_URL || "http://localhost:8092/api",
};

const endpoints = {
  auth: [
    {
      name: "Login",
      method: "POST",
      endpoint: "/public/login",
      hasBody: true,
      auth: "none",
    },
    {
      name: "Register",
      method: "POST",
      endpoint: "/public/register",
      hasBody: true,
      auth: "none",
    },
  ],

  authors: [
    {
      name: "Get Author By Name",
      method: "GET",
      endpoint: "/authors?name=Filipe",
      hasBody: false,
      auth: "reader",
    },
  ],

  books: [
    {
      name: "Search Books By Title",
      method: "GET",
      endpoint: "/books?title=Windhaven",
      hasBody: false,
      auth: "reader",
    },
    {
      name: "Search Books By Genre",
      method: "GET",
      endpoint: "/books?genre=Fantasia",
      hasBody: false,
      auth: "reader",
    },
    {
      name: "Get Book By ISBN",
      method: "GET",
      endpoint: "/books/9789720706386",
      hasBody: false,
      auth: "reader",
    },
  ],

  genres: [
    {
      name: "Top 5 Genres",
      method: "GET",
      endpoint: "/genres/top5",
      hasBody: false,
      auth: "librarian",
    },
    {
      name: "Lendings Per Month",
      method: "GET",
      endpoint: "/genres/lendingsPerMonthLastTwelveMonths",
      hasBody: false,
      auth: "librarian",
    },
    {
      name: "Average Lending Duration",
      method: "GET",
      endpoint: "/genres/lendingsAverageDurationPerMonth?startDate=2025-01-01&endDate=2026-12-31",
      hasBody: false,
      auth: "librarian",
    },
  ],

  lendings: [
    {
      name: "Lendings Per Month",
      method: "GET",
      endpoint: "/lendings/2024/1",
      hasBody: false,
      auth: "librarian",
    },
  ],

  readers: [
    {
      name: "List Readers",
      method: "GET",
      endpoint: "/readers",
      hasBody: false,
      auth: "librarian",
    },
    {
      name: "Readers Per Month",
      method: "GET",
      endpoint: "/readers/2026/1",
      hasBody: false,
      auth: "librarian",
    },
    {
      name: "Search Reader",
      method: "GET",
      endpoint: "/readers?name=Manuel",
      hasBody: false,
      auth: "librarian",
    },
  ],
};

const MODULES_ENV = (__ENV.MODULES || "books").trim();
const MODULES_TO_TEST =
  MODULES_ENV.toUpperCase() === "ALL"
    ? Object.keys(endpoints)
    : MODULES_ENV.split(",").map((m) => m.trim());

export const options = {
  scenarios: {
    ramp: {
      executor: "ramping-arrival-rate",
      startRate: 50,
      timeUnit: "1s",
      preAllocatedVUs: 50,
      maxVUs: 1200,
      stages: [
        { target: 100, duration: "1m" },
        { target: 1000, duration: "2m" },
        { target: 1000, duration: "2m" },
      ],
    },
  },
};

const AUTH_CREDS = {
  reader: { username: "manuel@gmail.com", password: "Manuelino123!" },
  librarian: { username: "maria@gmail.com", password: "Mariaroberta!123" },
};

function resolveAuthBaseUrl() {
  return ARCH === "monolith" ? MONOLITH_BASE_URL : MICRO_BASE_URLS.auth;
}

export function setup() {
  const authBaseUrl = resolveAuthBaseUrl();
  const tokens = {};

  ["reader", "librarian"].forEach((role) => {
    const res = http.post(
      `${authBaseUrl}/public/login`,
      JSON.stringify(AUTH_CREDS[role]),
      { headers: { "Content-Type": "application/json" } }
    );

    check(res, { [`${role} login ok`]: (r) => r.status === 200 });
    tokens[role] = res.headers.Authorization;
  });

  return { tokens };
}

function resolveBaseUrl(module) {
  return ARCH === "monolith" ? MONOLITH_BASE_URL : MICRO_BASE_URLS[module];
}

function buildHeaders(auth, tokens) {
  if (auth === "none") {
    return { "Content-Type": "application/json" };
  }
  return {
    "Content-Type": "application/json",
    Authorization: `Bearer ${tokens[auth]}`,
  };
}

function buildBody(name) {
  switch (name) {
    case "Login":
      return JSON.stringify(AUTH_CREDS.reader);
    case "Register":
      return JSON.stringify({
        name: "Test User",
        username: `testuser_vu${__VU}_iter${__ITER}@example.com`,
        password: "TestPassword123!",
        birthDate: "1990-01-01",
        phoneNumber: "1234567890",
        role: "READER",
        gpdr: true,
      });
    case "Create Author":
      return JSON.stringify({ name: "John Doe" });
    case "Create Lending":
      return JSON.stringify({ isbn: "9789720706386", readerNumber: "2026/1" });
    case "Create Book":
      return JSON.stringify({ title: "Title", genre: "Fantasia", authors: ["1"] });
    case "Create Reader":
      return JSON.stringify({ name: "Alice" });
    default:
      return null;
  }
}

const selectedEndpoints = MODULES_TO_TEST.flatMap(
  (module) => endpoints[module]?.map((e) => ({ module, ...e })) || []
);

function executeEndpoint(cfg, data) {
  const baseUrl = resolveBaseUrl(cfg.module);
  const url = `${baseUrl}${cfg.endpoint}`;
  const headers = buildHeaders(cfg.auth, data.tokens);
  const body = cfg.hasBody ? buildBody(cfg.name) : null;

  const res =
    cfg.method === "GET"
      ? http.get(url, { headers })
      : http.request(cfg.method, url, body, { headers });

  if (res.status !== 200) console.log(res);

  check(res, {
    [`${cfg.module} | ${cfg.name} | status < 400`]: (r) =>
      r.status >= 200 && r.status < 400,
  });
}

export default function (data) {
  const idx = Math.floor(Math.random() * selectedEndpoints.length);
  executeEndpoint(selectedEndpoints[idx], data);
  sleep(0.1);
}
