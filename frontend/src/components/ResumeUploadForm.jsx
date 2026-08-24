import { useState } from 'react';

export default function ResumeUploadForm({ onSubmit, submitting }) {
  const [candidateName, setCandidateName] = useState('');
  const [email, setEmail] = useState('');
  const [file, setFile] = useState(null);

  function handleSubmit(e) {
    e.preventDefault();
    if (!file || !candidateName.trim() || !email.trim()) {
      return;
    }
    onSubmit({ candidateName: candidateName.trim(), email: email.trim(), file });
    setCandidateName('');
    setEmail('');
    setFile(null);
    e.target.reset();
  }

  return (
    <form onSubmit={handleSubmit} aria-label="Upload resume">
      <div className="field">
        <label htmlFor="candidateName">Candidate name</label>
        <input
          id="candidateName"
          type="text"
          value={candidateName}
          onChange={(e) => setCandidateName(e.target.value)}
          required
        />
      </div>
      <div className="field">
        <label htmlFor="email">Email</label>
        <input
          id="email"
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
      </div>
      <div className="field">
        <label htmlFor="resumeFile">Resume (PDF or TXT)</label>
        <input
          id="resumeFile"
          type="file"
          accept=".pdf,.txt"
          onChange={(e) => setFile(e.target.files[0] ?? null)}
          required
        />
      </div>
      <button type="submit" disabled={submitting}>
        {submitting ? 'Uploading...' : 'Upload resume'}
      </button>
    </form>
  );
}
