import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import ScoreBadge from '../../src/components/ScoreBadge.jsx';

describe('ScoreBadge', () => {
  it.each([
    [9, 'score-badge--high'],
    [5, 'score-badge--medium'],
    [2, 'score-badge--low'],
  ])('applies the correct tier class for score %i', (score, expectedClass) => {
    render(<ScoreBadge score={score} />);
    expect(screen.getByLabelText(`Score ${score} out of 10`)).toHaveClass(expectedClass);
  });
});
