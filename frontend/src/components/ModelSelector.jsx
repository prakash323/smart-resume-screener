export default function ModelSelector({ label, suggested, value, onChange }) {
  const isCustom = value !== '' && !suggested.includes(value);

  return (
    <div className="field">
      <label>{label}</label>
      <select
        value={isCustom ? '__custom__' : value}
        onChange={(e) => {
          if (e.target.value === '__custom__') {
            onChange('');
          } else {
            onChange(e.target.value);
          }
        }}
      >
        {suggested.map((model) => (
          <option key={model} value={model}>
            {model}
          </option>
        ))}
        <option value="__custom__">Custom model slug...</option>
      </select>
      {isCustom || value === '' ? (
        <input
          type="text"
          placeholder="e.g. mistralai/mixtral-8x7b-instruct"
          value={value}
          onChange={(e) => onChange(e.target.value)}
        />
      ) : null}
    </div>
  );
}
