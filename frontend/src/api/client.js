const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

async function handleResponse(response) {
  if (!response.ok) {
    let message = `Request failed with status ${response.status}`;
    try {
      const body = await response.json();
      message = body.message || message;
    } catch {
      // response body wasn't JSON, keep the generic message
    }
    throw new Error(message);
  }
  return response.status === 204 ? null : response.json();
}

export async function getModels() {
  const response = await fetch(`${API_BASE_URL}/models`);
  return handleResponse(response);
}

export async function listResumes() {
  const response = await fetch(`${API_BASE_URL}/resumes`);
  return handleResponse(response);
}

export async function uploadResume({ file, candidateName, email, model }) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('candidateName', candidateName);
  formData.append('email', email);
  if (model) {
    formData.append('model', model);
  }

  const response = await fetch(`${API_BASE_URL}/resumes`, {
    method: 'POST',
    body: formData,
  });
  return handleResponse(response);
}

export async function listJobDescriptions() {
  const response = await fetch(`${API_BASE_URL}/job-descriptions`);
  return handleResponse(response);
}

export async function createJobDescription({ title, rawText }) {
  const response = await fetch(`${API_BASE_URL}/job-descriptions`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ title, rawText }),
  });
  return handleResponse(response);
}

export async function runScreening({ resumeId, jobDescriptionId, model }) {
  const response = await fetch(`${API_BASE_URL}/screenings`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ resumeId, jobDescriptionId, model: model || undefined }),
  });
  return handleResponse(response);
}

export async function getShortlist({ jobDescriptionId, minScore }) {
  const params = new URLSearchParams({ jobDescriptionId });
  if (minScore !== undefined && minScore !== null && minScore !== '') {
    params.set('minScore', minScore);
  }
  const response = await fetch(`${API_BASE_URL}/screenings/shortlist?${params.toString()}`);
  return handleResponse(response);
}
