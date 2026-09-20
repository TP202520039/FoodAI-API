package com.tp.foodai.shared.interfaces.rest.middleware;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Per-request accumulator of stage durations, exposed as a {@code Server-Timing} header by
 * {@link ServerTimingFilter}. Thread-bound (one HTTP request = one servlet thread).
 *
 * Added for the end-to-end latency benchmark requested by ICACIT Reviewer 1 (rigor:
 * N&gt;=100 trials, device, network, percentiles, CPU/GPU). See
 * paper/benchmark_latencia/PROTOCOLO.md in the FoodAI-Paper repo for the full protocol.
 *
 * Usage inside FoodDetectionServiceImpl.analyzeFood:
 *
 *   String url = ServerTiming.stage("blob", () -> azureBlobStorageService.uploadImage(file, uid));
 *   AiDetectionResponseDto r = ServerTiming.stage("ai", () -> aiDetectionService.detectFood(url));
 *   ServerTiming.mergeUpstream(aiDetectionService.getLastServerTiming(), "ai_"); // propagate FastAPI stages
 *   List&lt;FoodComponent&gt; c = ServerTiming.stage("db_lookup", () -> enrichComponents(r.getDetectedFoods(), fd));
 *   FoodDetection saved   = ServerTiming.stage("db_persist", () -> foodDetectionRepository.save(fd));
 *
 * Nothing changes for production clients: they just get one extra response header.
 */
public final class ServerTiming {

    private static final ThreadLocal<Map<String, Double>> STAGES =
            ThreadLocal.withInitial(LinkedHashMap::new);

    private ServerTiming() {}

    /** Time a block and accumulate it under {@code name} (ms). */
    public static <T> T stage(String name, Supplier<T> block) {
        long t0 = System.nanoTime();
        try {
            return block.get();
        } finally {
            add(name, (System.nanoTime() - t0) / 1e6);
        }
    }

    public static void stage(String name, Runnable block) {
        stage(name, () -> { block.run(); return null; });
    }

    public static void add(String name, double ms) {
        STAGES.get().merge(name, ms, Double::sum);
    }

    /**
     * Merge an upstream {@code Server-Timing} header (e.g. from FastAPI) with a prefix, so
     * {@code inference;dur=180.7} becomes {@code ai_inference;dur=180.7}.
     */
    public static void mergeUpstream(String serverTimingHeader, String prefix) {
        if (serverTimingHeader == null || serverTimingHeader.isBlank()) return;
        for (String part : serverTimingHeader.split(",")) {
            String[] segs = part.trim().split(";");
            String name = segs[0].trim();
            for (int i = 1; i < segs.length; i++) {
                String[] kv = segs[i].trim().split("=", 2);
                if (kv.length == 2 && kv[0].equals("dur")) {
                    try { add(prefix + name, Double.parseDouble(kv[1])); }
                    catch (NumberFormatException ignored) {}
                }
            }
        }
    }

    static String render(double totalMs) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Double> e : STAGES.get().entrySet()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(e.getKey()).append(";dur=").append(String.format("%.2f", e.getValue()));
        }
        if (sb.length() > 0) sb.append(", ");
        sb.append("total;dur=").append(String.format("%.2f", totalMs));
        return sb.toString();
    }

    static void reset() {
        STAGES.remove();
    }
}
