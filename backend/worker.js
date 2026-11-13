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

    const models = ["gemini-3.8-flash", "gemini-3.8-flash-lite", "gemini-2.0-flash"];
    let lastStatus = 0;
    let lastDetail = "";

    for (const model of models) {
      for (let attempt = 0; attempt < 2; attempt++) {
        let res;
        try {
          res = await fetch(
            "https://generativelanguage.googleapis.com/v1beta/models/" +
              model + ":generateContent",
            {
              method: "POST",
              headers: {
                "Content-Type": "application/json",
                "x-goog-api-key": env.GEMINI_API_KEY || "",
              },
              body: JSON.stringify(payload),
            }
          );
        } catch (e) {
          lastStatus = 0;
          lastDetail = "network";
          continue;
        }

        if (res.ok) {
          const data = await res.json();
          const text =
            data?.candidates?.[0]?.content?.parts?.map((p) => p.text || "").join("") || "";
          if (text) return json({ text, model }, 200, cors);
          lastDetail = "empty";
          break;
        }

        lastStatus = res.status;
        lastDetail = (await res.text()).slice(0, 200);
        if (res.status === 503 || res.status === 429) {
          continue; // miezaka indray, avy eo modely manaraka
        }
        break; // 404/400/403: handeha any amin'ny modely manaraka
      }
    }

    return json(
      { error: "AI service error", status: lastStatus, detail: lastDetail },
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
