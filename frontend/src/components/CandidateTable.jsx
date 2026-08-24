import ScoreBadge from './ScoreBadge.jsx';

export default function CandidateTable({ entries }) {
  if (entries.length === 0) {
    return <p className="empty-state">No screened candidates yet for this job description.</p>;
  }

  return (
    <table className="candidate-table">
      <thead>
        <tr>
          <th>Candidate</th>
          <th>Email</th>
          <th>Score</th>
          <th>Justification</th>
          <th>Model</th>
        </tr>
      </thead>
      <tbody>
        {entries.map((entry) => (
          <tr key={`${entry.resumeId}-${entry.modelUsed}`}>
            <td>{entry.candidateName}</td>
            <td>{entry.email}</td>
            <td>
              <ScoreBadge score={entry.score} />
            </td>
            <td>{entry.justification}</td>
            <td className="model-cell">{entry.modelUsed}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
