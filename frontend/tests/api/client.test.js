import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { uploadResume, createJobDescription, runScreening, getShortlist } from '../../src/api/client.js';

describe('api client', () => {
  beforeEach(() => {
    global.fetch = vi.fn();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('uploadResume sends multipart form data and returns parsed JSON', async () => {
    const responsePayload = { id: 1, candidateName: 'Jane Doe' };
    global.fetch.mockResolvedValue({
      ok: true,
      status: 201,
      json: async () => responsePayload,
    });

    const file = new File(['resume text'], 'resume.txt', { type: 'text/plain' });
    const result = await uploadResume({ file, candidateName: 'Jane Doe', email: 'jane@example.com', model: 'openai/gpt-4o-mini' });

    expect(result).toEqual(responsePayload);
    expect(global.fetch).toHaveBeenCalledTimes(1);
    const [url, options] = global.fetch.mock.calls[0];
    expect(url).toContain('/resumes');
    expect(options.method).toBe('POST');
    expect(options.body).toBeInstanceOf(FormData);
  });

  it('createJobDescription posts JSON body', async () => {
    const responsePayload = { id: 2, title: 'Backend Engineer' };
    global.fetch.mockResolvedValue({
      ok: true,
      status: 201,
      json: async () => responsePayload,
    });

    const result = await createJobDescription({ title: 'Backend Engineer', rawText: 'Some JD text' });

    expect(result).toEqual(responsePayload);
    const [, options] = global.fetch.mock.calls[0];
    expect(JSON.parse(options.body)).toEqual({ title: 'Backend Engineer', rawText: 'Some JD text' });
  });

  it('runScreening posts resumeId, jobDescriptionId and model', async () => {
    global.fetch.mockResolvedValue({
      ok: true,
      status: 201,
      json: async () => ({ score: 8 }),
    });

    await runScreening({ resumeId: 1, jobDescriptionId: 2, model: 'openai/gpt-4o-mini' });

    const [, options] = global.fetch.mock.calls[0];
    expect(JSON.parse(options.body)).toEqual({ resumeId: 1, jobDescriptionId: 2, model: 'openai/gpt-4o-mini' });
  });

  it('getShortlist builds query params from jobDescriptionId and minScore', async () => {
    global.fetch.mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => [],
    });

    await getShortlist({ jobDescriptionId: 5, minScore: 7 });

    const [url] = global.fetch.mock.calls[0];
    expect(url).toContain('jobDescriptionId=5');
    expect(url).toContain('minScore=7');
  });

  it('throws an error with the backend message when the response is not ok', async () => {
    global.fetch.mockResolvedValue({
      ok: false,
      status: 404,
      json: async () => ({ error: 'NOT_FOUND', message: 'Resume not found: 999' }),
    });

    await expect(getShortlist({ jobDescriptionId: 999 })).rejects.toThrow('Resume not found: 999');
  });
});
