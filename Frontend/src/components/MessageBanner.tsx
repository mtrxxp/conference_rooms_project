import { useAppContext } from "../context/AppContext";

export function MessageBanner() {
  const { message, clearMessage } = useAppContext();

  if (!message) {
    return null;
  }

  return (
    <div
      className={`alert ${message.type === "success" ? "ok" : "error"}`}
      role="status"
    >
      <span>{message.text}</span>
      <button
        type="button"
        className="icon-button"
        onClick={clearMessage}
        aria-label="Close message"
      >
        x
      </button>
    </div>
  );
}
