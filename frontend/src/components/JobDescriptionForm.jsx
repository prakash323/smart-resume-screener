import { useState } from 'react';

export default function JobDescriptionForm({ onSubmit, submitting }) {
  const [title, setTitle] = useState('');
  const [rawText, setRawText] = useState('');

  function handleSubmit(e) {
    e.preventDefault();
    if (!title.trim() || !rawText.trim()) {
      return;
    }
    onSubmit({ title: title.trim(), rawText: rawText.trim() });
    setTitle('');
    setRawText('');
  }

  return (
    <form onSubmit={handleSubmit} aria-label="Create job description">
      <div className="field">
        <label htmlFor="jdTitle">Job title</label>
        <input id="jdTitle" type="text" value={title} onChange={(e) => setTitle(e.target.value)} required />
      </div>
      <div className="field">
        <label htmlFor="jdText">Job description</label>
        <textarea
          id="jdText"
          rows={6}
          value={rawText}
          onChange={(e) => setRawText(e.target.value)}
          required
        />
      </div>
      <button type="submit" disabled={submitting}>
        {submitting ? 'Saving...' : 'Save job description'}
      </button>
    </form>
  );
}
