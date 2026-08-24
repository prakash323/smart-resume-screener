import { useEffect, useState } from 'react';
import ResumeUploadForm from './components/ResumeUploadForm.jsx';
import JobDescriptionForm from './components/JobDescriptionForm.jsx';
import ModelSelector from './components/ModelSelector.jsx';
import CandidateTable from './components/CandidateTable.jsx';
import {
  getModels,
  listResumes,
  uploadResume,
  listJobDescriptions,
  createJobDescription,
  runScreening,
  getShortlist,
} from './api/client.js';

export default function App() {
  const [resumes, setResumes] = useState([]);
  const [jobDescriptions, setJobDescriptions] = useState([]);
  const [selectedJdId, setSelectedJdId] = useState('');
  const [shortlist, setShortlist] = useState([]);
  const [suggestedModels, setSuggestedModels] = useState([]);
  const [model, setModel] = useState('');
  const [minScore, setMinScore] = useState('');
  const [uploading, setUploading] = useState(false);
  const [creatingJd, setCreatingJd] = useState(false);
  const [screeningResumeId, setScreeningResumeId] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    getModels()
      .then((data) => {
        setSuggestedModels(data.suggested);
        setModel(data.defaultModel);
      })
      .catch((err) => setError(err.message));

    listResumes().then(setResumes).catch((err) => setError(err.message));
    listJobDescriptions().then(setJobDescriptions).catch((err) => setError(err.message));
  }, []);

  useEffect(() => {
    if (!selectedJdId) {
      setShortlist([]);
      return;
    }
    refreshShortlist(selectedJdId);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedJdId]);

  function refreshShortlist(jdId) {
    getShortlist({ jobDescriptionId: jdId, minScore: minScore || undefined })
      .then(setShortlist)
      .catch((err) => setError(err.message));
  }

  async function handleUploadResume(values) {
    setUploading(true);
    setError(null);
    try {
      const resume = await uploadResume({ ...values, model });
      setResumes((prev) => [...prev, resume]);
    } catch (err) {
      setError(err.message);
    } finally {
      setUploading(false);
    }
  }

  async function handleCreateJobDescription(values) {
    setCreatingJd(true);
    setError(null);
    try {
      const jd = await createJobDescription(values);
      setJobDescriptions((prev) => [...prev, jd]);
      setSelectedJdId(String(jd.id));
    } catch (err) {
      setError(err.message);
    } finally {
      setCreatingJd(false);
    }
  }

  async function handleScreen(resumeId) {
    if (!selectedJdId) {
      setError('Select a job description before screening a candidate.');
      return;
    }
    setScreeningResumeId(resumeId);
    setError(null);
    try {
      await runScreening({ resumeId, jobDescriptionId: Number(selectedJdId), model });
      refreshShortlist(selectedJdId);
    } catch (err) {
      setError(err.message);
    } finally {
      setScreeningResumeId(null);
    }
  }

  async function handleScreenAll() {
    if (!selectedJdId) {
      setError('Select a job description before screening candidates.');
      return;
    }
    setError(null);
    for (const resume of resumes) {
      try {
        // eslint-disable-next-line no-await-in-loop
        await runScreening({ resumeId: resume.id, jobDescriptionId: Number(selectedJdId), model });
      } catch (err) {
        setError(err.message);
      }
    }
    refreshShortlist(selectedJdId);
  }

  return (
    <div className="app">
      <header className="app-header">
        <h1>Smart Resume Screener</h1>
        <p>Parse resumes, extract structured data, and score candidate fit against a job description via an LLM.</p>
      </header>

      {error ? <div className="error-banner">{error}</div> : null}

      <ModelSelector label="LLM model (OpenRouter)" suggested={suggestedModels} value={model} onChange={setModel} />

      <div className="columns">
        <section className="panel">
          <h2>1. Upload resumes</h2>
          <ResumeUploadForm onSubmit={handleUploadResume} submitting={uploading} />

          <h3>Uploaded candidates ({resumes.length})</h3>
          <ul className="resume-list">
            {resumes.map((resume) => (
              <li key={resume.id}>
                <div>
                  <strong>{resume.candidateName}</strong> - {resume.email}
                  <div className="skills-preview">
                    {resume.extractedData.skills.slice(0, 6).join(', ') || 'No skills extracted'}
                  </div>
                </div>
                <button
                  onClick={() => handleScreen(resume.id)}
                  disabled={screeningResumeId === resume.id || !selectedJdId}
                >
                  {screeningResumeId === resume.id ? 'Screening...' : 'Screen against JD'}
                </button>
              </li>
            ))}
          </ul>
        </section>

        <section className="panel">
          <h2>2. Job description</h2>
          <JobDescriptionForm onSubmit={handleCreateJobDescription} submitting={creatingJd} />

          <div className="field">
            <label htmlFor="jdSelect">Active job description</label>
            <select id="jdSelect" value={selectedJdId} onChange={(e) => setSelectedJdId(e.target.value)}>
              <option value="">-- select a job description --</option>
              {jobDescriptions.map((jd) => (
                <option key={jd.id} value={jd.id}>
                  {jd.title}
                </option>
              ))}
            </select>
          </div>

          <button onClick={handleScreenAll} disabled={!selectedJdId || resumes.length === 0}>
            Screen all uploaded candidates
          </button>
        </section>
      </div>

      <section className="panel">
        <h2>3. Shortlist</h2>
        <div className="field field--inline">
          <label htmlFor="minScore">Minimum score</label>
          <input
            id="minScore"
            type="number"
            min="1"
            max="10"
            value={minScore}
            onChange={(e) => setMinScore(e.target.value)}
          />
          <button onClick={() => selectedJdId && refreshShortlist(selectedJdId)} disabled={!selectedJdId}>
            Refresh
          </button>
        </div>
        <CandidateTable entries={shortlist} />
      </section>
    </div>
  );
}
