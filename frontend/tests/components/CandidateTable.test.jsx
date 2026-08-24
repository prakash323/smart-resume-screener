import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import CandidateTable from '../../src/components/CandidateTable.jsx';

describe('CandidateTable', () => {
  it('renders an empty state when there are no entries', () => {
    render(<CandidateTable entries={[]} />);

    expect(screen.getByText(/no screened candidates/i)).toBeInTheDocument();
  });

  it('renders a row per candidate with score and justification', () => {
    const entries = [
      { resumeId: 1, candidateName: 'Alice', email: 'alice@example.com', score: 9, justification: 'Great fit', modelUsed: 'openai/gpt-4o-mini' },
      { resumeId: 2, candidateName: 'Bob', email: 'bob@example.com', score: 3, justification: 'Weak fit', modelUsed: 'openai/gpt-4o-mini' },
    ];

    render(<CandidateTable entries={entries} />);

    expect(screen.getByText('Alice')).toBeInTheDocument();
    expect(screen.getByText('9/10')).toBeInTheDocument();
    expect(screen.getByText('Great fit')).toBeInTheDocument();
    expect(screen.getByText('Bob')).toBeInTheDocument();
    expect(screen.getByText('3/10')).toBeInTheDocument();
  });
});
