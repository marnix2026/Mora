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
        JSON.stringify({ ok: true, service: "mora-ai", hasKey: !!env.GEMINI_API_KEY }),
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
    const messages = Array.isArray(body.messages) ? body.messages.slice(-12) : [];
    if (messages.length === 0) {
      return json({ error: "No messages" }, 400, cors);
    }

    const contents = messages.map((m) => ({
      role: m.role === "assistant" ? "model" : "user",
      parts: [{ text: String(m.text || "").slice(0, 4000) }],
    }));

    const models = ["gemini-3.5-flash-lite", "gemini-3.1-flash-lite"];
    const errors = [];

    for (const name of models) {
      const payload = { contents, generationConfig: { maxOutputTokens: 800 } };
      if (system) payload.systemInstruction = { parts: [{ text: system }] };
      try {
        const res = await fetch(
          "https://generativelanguage.googleapis.com/v1beta/models/" +
            name + ":generateContent",
          {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              "x-goog-api-key": env.GEMINI_API_KEY || "",
            },
            body: JSON.stringify(payload),
            signal: AbortSignal.timeout(20000),
          }
        );
        if (res.ok) {
          const data = await res.json();
          const text =
            data?.candidates?.[0]?.content?.parts?.map((p) => p.text || "").join("") || "";
          if (text) return json({ text, model: name }, 200, cors);
          errors.push(name + ": empty");
        } else {
          errors.push(name + ": " + res.status);
          if (res.status === 429) break;
        }
      } catch (e) {
        errors.push(name + ": timeout");
      }
    }

    return json(
      { error: "AI service error", status: 0, detail: errors.join(" | ") },
      502,
      cors
    );
  },
};

function json(obj, status, cors) {
  return new Response(JSON.stringify(obj), {
    status,
    headers: { ...cors, "Content-Type": "application/json" },
  });
}
