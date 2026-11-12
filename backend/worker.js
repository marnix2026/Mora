export default {
  async fetch(request, env) {
    const cors = {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "POST, OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type",
    };

    if (request.method === "OPTIONS") {
      return new Response(null, { headers: cors });
    }

    if (request.method !== "POST") {
      return new Response(
        JSON.stringify({ ok: true, service: "mora-ai" }),
        { headers: { ...cors, "Content-Type": "application/json" } }
      );
    }

    let body;
    try {
      body = await request.json();
    } catch (e) {
      return json({ error: "Invalid JSON" }, 400, cors);
    }

    const system = String(body.system || "").slice(0, 4000);
    const messages = Array.isArray(body.messages) ? body.messages.slice(-30) : [];
    if (messages.length === 0) {
      return json({ error: "No messages" }, 400, cors);
    }

    const contents = messages.map((m) => ({
      role: m.role === "assistant" ? "model" : "user",
      parts: [{ text: String(m.text || "").slice(0, 8000) }],
    }));

    const payload = { contents };
    if (system) {
      payload.systemInstruction = { parts: [{ text: system }] };
    }

    const model = "gemini-2.5-flash";
    const url =
      "https://generativelanguage.googleapis.com/v1beta/models/" +
      model + ":generateContent";

    let res;
    try {
      res = await fetch(url, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "x-goog-api-key": env.GEMINI_API_KEY,
        },
        body: JSON.stringify(payload),
      });
    } catch (e) {
      return json({ error: "Network error to AI service" }, 502, cors);
    }

    if (!res.ok) {
      return json({ error: "AI service error", status: res.status }, 502, cors);
    }

    const data = await res.json();
    const text =
      data?.candidates?.[0]?.content?.parts?.map((p) => p.text || "").join("") || "";

    if (!text) {
      return json({ error: "Empty AI response" }, 502, cors);
    }
    return json({ text }, 200, cors);
  },
};

function json(obj, status, cors) {
  return new Response(JSON.stringify(obj), {
    status,
    headers: { ...cors, "Content-Type": "application/json" },
  });
}
