export default function ScoreBadge({ score }) {
  let tier = 'low';
  if (score >= 7) {
    tier = 'high';
  } else if (score >= 4) {
    tier = 'medium';
  }

  return (
    <span className={`score-badge score-badge--${tier}`} aria-label={`Score ${score} out of 10`}>
      {score}/10
    </span>
  );
}
