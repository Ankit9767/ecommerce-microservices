import { apiGet } from "./api";

function getAllAudits({
  page = 0,
  size = 20,
  sort
} = {}) {
  const params = new URLSearchParams();

  params.set("page", page);
  params.set("size", size);

  if (sort) {
    params.set("sort", sort);
  }

  return apiGet(`/admin/audit?${params.toString()}`);
}

function getAuditsByUser(
  userId,
  {
    page = 0,
    size = 20,
    sort
  } = {}
) {
  const params = new URLSearchParams();

  params.set("page", page);
  params.set("size", size);

  if (sort) {
    params.set("sort", sort);
  }

  return apiGet(
    `/admin/audit/user/${userId}?${params.toString()}`
  );
}

function getAuditsByEventType(
  eventType,
  {
    page = 0,
    size = 20,
    sort
  } = {}
) {
  const params = new URLSearchParams();

  params.set("page", page);
  params.set("size", size);

  if (sort) {
    params.set("sort", sort);
  }

  return apiGet(
    `/admin/audit/event/${eventType}?${params.toString()}`
  );
}

export {
  getAllAudits,
  getAuditsByUser,
  getAuditsByEventType
};