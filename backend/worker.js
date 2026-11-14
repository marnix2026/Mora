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

    const candidates = [
      { name: "gemini-2.5-flash-lite", cfg: { maxOutputTokens: 800 } },
      { name: "gemini-4-hydrogen", cfg: { thinkingConfig: { thinkingLevel: "low" }, maxOutputTokens: 800 } },
      { name: "gemini-3.8-flash", cfg: { thinkingConfig: { thinkingLevel: "low" }, maxOutputTokens: 800 } },
    ];

    const errors = [];
    for (const c of candidates) {
      const payload = { contents, generationConfig: c.cfg };
      if (system) payload.systemInstruction = { parts: [{ text: system }] };
      try {
        const res = await fetch(
          "https://generativelanguage.googleapis.com/v1beta/models/" +
            c.name + ":generateContent",
          {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              "x-goog-api-key": env.GEMINI_API_KEY || "",
            },
            body: JSON.stringify(payload),
            signal: AbortSignal.timeout(12000),
          }
        );
        if (res.ok) {
          const data = await res.json();
          const text =
            data?.candidates?.[0]?.content?.parts?.map((p) => p.text || "").join("") || "";
          if (text) return json({ text, model: c.name }, 200, cors);
          errors.push(c.name + ": empty");
        } else {
          errors.push(c.name + ": " + res.status);
        }
      } catch (e) {
        errors.push(c.name + ": timeout");
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
