import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import ResumeUploadForm from '../../src/components/ResumeUploadForm.jsx';

describe('ResumeUploadForm', () => {
  it('calls onSubmit with candidate name, email and file when submitted', () => {
    const handleSubmit = vi.fn();
    render(<ResumeUploadForm onSubmit={handleSubmit} submitting={false} />);

    fireEvent.change(screen.getByLabelText(/candidate name/i), { target: { value: 'Jane Doe' } });
    fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'jane@example.com' } });

    const file = new File(['resume content'], 'resume.pdf', { type: 'application/pdf' });
    fireEvent.change(screen.getByLabelText(/resume \(pdf or txt\)/i), { target: { files: [file] } });

    // Submitting via the button click goes through native HTML5 required-field
    // validation in jsdom; dispatch the submit event directly to bypass that
    // and exercise the component's own onSubmit handler.
    fireEvent.submit(screen.getByRole('form', { name: /upload resume/i }));

    expect(handleSubmit).toHaveBeenCalledWith({
      candidateName: 'Jane Doe',
      email: 'jane@example.com',
      file,
    });
  });

  it('does not call onSubmit when required fields are missing', () => {
    const handleSubmit = vi.fn();
    render(<ResumeUploadForm onSubmit={handleSubmit} submitting={false} />);

    fireEvent.click(screen.getByRole('button', { name: /upload resume/i }));

    expect(handleSubmit).not.toHaveBeenCalled();
  });

  it('disables the submit button while submitting', () => {
    render(<ResumeUploadForm onSubmit={vi.fn()} submitting={true} />);

    expect(screen.getByRole('button', { name: /uploading/i })).toBeDisabled();
  });
});
