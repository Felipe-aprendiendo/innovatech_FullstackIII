package cl.innovatech.reports.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EntityTest {

    // ─── KpiSnapshot Tests ─────────────────────────────────────────

    @Test
    void kpiSnapshot_builder_inicializaTodosLosCampos() {
        LocalDateTime now = LocalDateTime.now();

        KpiSnapshot snapshot = KpiSnapshot.builder()
                .id(1L)
                .projectId(10L)
                .totalTareas(20)
                .tareasCompletadas(10)
                .tareasEnProgreso(5)
                .tareasPendientes(5)
                .porcentajeAvance(new BigDecimal("50.00"))
                .calculadoAt(now)
                .build();

        assertThat(snapshot.getId()).isEqualTo(1L);
        assertThat(snapshot.getProjectId()).isEqualTo(10L);
        assertThat(snapshot.getTotalTareas()).isEqualTo(20);
        assertThat(snapshot.getTareasCompletadas()).isEqualTo(10);
        assertThat(snapshot.getTareasEnProgreso()).isEqualTo(5);
        assertThat(snapshot.getTareasPendientes()).isEqualTo(5);
        assertThat(snapshot.getPorcentajeAvance()).isEqualByComparingTo("50.00");
        assertThat(snapshot.getCalculadoAt()).isEqualTo(now);
    }

    @Test
    void kpiSnapshot_noArgsConstructor_creaInstanciaVacia() {
        KpiSnapshot snapshot = new KpiSnapshot();

        assertThat(snapshot.getId()).isNull();
        assertThat(snapshot.getProjectId()).isNull();
        assertThat(snapshot.getTotalTareas()).isNull();
        assertThat(snapshot.getTareasCompletadas()).isNull();
        assertThat(snapshot.getTareasEnProgreso()).isNull();
        assertThat(snapshot.getTareasPendientes()).isNull();
        assertThat(snapshot.getPorcentajeAvance()).isNull();
    }

    @Test
    void kpiSnapshot_allArgsConstructor_inicializaTodosLosCampos() {
        LocalDateTime now = LocalDateTime.now();

        KpiSnapshot snapshot = new KpiSnapshot(1L, 10L, 20, 10, 5, 5,
                new BigDecimal("50.00"), now);

        assertThat(snapshot.getId()).isEqualTo(1L);
        assertThat(snapshot.getProjectId()).isEqualTo(10L);
        assertThat(snapshot.getTotalTareas()).isEqualTo(20);
    }

    @Test
    void kpiSnapshot_setters_actualizanCampos() {
        KpiSnapshot snapshot = new KpiSnapshot();

        snapshot.setId(2L);
        snapshot.setProjectId(20L);
        snapshot.setTotalTareas(30);
        snapshot.setTareasCompletadas(15);
        snapshot.setTareasEnProgreso(8);
        snapshot.setTareasPendientes(7);
        snapshot.setPorcentajeAvance(new BigDecimal("75.00"));
        snapshot.setCalculadoAt(LocalDateTime.now());

        assertThat(snapshot.getId()).isEqualTo(2L);
        assertThat(snapshot.getProjectId()).isEqualTo(20L);
        assertThat(snapshot.getTotalTareas()).isEqualTo(30);
        assertThat(snapshot.getTareasCompletadas()).isEqualTo(15);
        assertThat(snapshot.getTareasEnProgreso()).isEqualTo(8);
        assertThat(snapshot.getTareasPendientes()).isEqualTo(7);
        assertThat(snapshot.getPorcentajeAvance()).isEqualByComparingTo("75.00");
        assertThat(snapshot.getCalculadoAt()).isNotNull();
    }

    @Test
    void kpiSnapshot_equals_conMismosValores_sonIguales() {
        LocalDateTime now = LocalDateTime.now();

        KpiSnapshot s1 = KpiSnapshot.builder().id(1L).projectId(10L)
                .totalTareas(5).tareasCompletadas(2).tareasEnProgreso(1)
                .tareasPendientes(2).porcentajeAvance(BigDecimal.ZERO).calculadoAt(now).build();

        KpiSnapshot s2 = KpiSnapshot.builder().id(1L).projectId(10L)
                .totalTareas(5).tareasCompletadas(2).tareasEnProgreso(1)
                .tareasPendientes(2).porcentajeAvance(BigDecimal.ZERO).calculadoAt(now).build();

        assertThat(s1).isEqualTo(s2);
    }

    @Test
    void kpiSnapshot_equals_conIdsDiferentes_noSonIguales() {
        KpiSnapshot s1 = KpiSnapshot.builder().id(1L).projectId(1L)
                .totalTareas(0).tareasCompletadas(0).tareasEnProgreso(0)
                .tareasPendientes(0).porcentajeAvance(BigDecimal.ZERO).build();

        KpiSnapshot s2 = KpiSnapshot.builder().id(2L).projectId(1L)
                .totalTareas(0).tareasCompletadas(0).tareasEnProgreso(0)
                .tareasPendientes(0).porcentajeAvance(BigDecimal.ZERO).build();

        assertThat(s1).isNotEqualTo(s2);
    }

    @Test
    void kpiSnapshot_hashCode_consistente() {
        LocalDateTime now = LocalDateTime.now();

        KpiSnapshot s1 = KpiSnapshot.builder().id(1L).projectId(10L)
                .totalTareas(5).tareasCompletadas(2).tareasEnProgreso(1)
                .tareasPendientes(2).porcentajeAvance(BigDecimal.ZERO).calculadoAt(now).build();

        KpiSnapshot s2 = KpiSnapshot.builder().id(1L).projectId(10L)
                .totalTareas(5).tareasCompletadas(2).tareasEnProgreso(1)
                .tareasPendientes(2).porcentajeAvance(BigDecimal.ZERO).calculadoAt(now).build();

        assertThat(s1.hashCode()).isEqualTo(s2.hashCode());
    }

    @Test
    void kpiSnapshot_toString_contieneInformacion() {
        KpiSnapshot snapshot = KpiSnapshot.builder()
                .id(1L).projectId(5L).totalTareas(10)
                .tareasCompletadas(4).tareasEnProgreso(3).tareasPendientes(3)
                .porcentajeAvance(new BigDecimal("40.00")).build();

        String str = snapshot.toString();
        assertThat(str).contains("KpiSnapshot");
        assertThat(str).contains("projectId=5");
    }

    @Test
    void kpiSnapshot_onCreate_asignaCalculadoAt() {
        KpiSnapshot snapshot = new KpiSnapshot();
        snapshot.onCreate();

        assertThat(snapshot.getCalculadoAt()).isNotNull();
        assertThat(snapshot.getCalculadoAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void kpiSnapshot_conPorcentajeCero_esValido() {
        KpiSnapshot snapshot = KpiSnapshot.builder()
                .id(1L).projectId(1L)
                .totalTareas(10).tareasCompletadas(0)
                .tareasEnProgreso(0).tareasPendientes(10)
                .porcentajeAvance(BigDecimal.ZERO).build();

        assertThat(snapshot.getPorcentajeAvance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void kpiSnapshot_conPorcentaje100_esValido() {
        KpiSnapshot snapshot = KpiSnapshot.builder()
                .id(1L).projectId(1L)
                .totalTareas(10).tareasCompletadas(10)
                .tareasEnProgreso(0).tareasPendientes(0)
                .porcentajeAvance(new BigDecimal("100.00")).build();

        assertThat(snapshot.getPorcentajeAvance()).isEqualByComparingTo("100.00");
    }

    // ─── ReportCache Tests ─────────────────────────────────────────

    @Test
    void reportCache_builder_inicializaTodosLosCampos() {
        LocalDateTime now = LocalDateTime.now();

        ReportCache cache = ReportCache.builder()
                .id(1L)
                .tipo("PROJECT_REPORT")
                .filtros("{\"projectId\":1}")
                .resultado("{\"data\":[]}")
                .generadoPor(5L)
                .createdAt(now)
                .build();

        assertThat(cache.getId()).isEqualTo(1L);
        assertThat(cache.getTipo()).isEqualTo("PROJECT_REPORT");
        assertThat(cache.getFiltros()).isEqualTo("{\"projectId\":1}");
        assertThat(cache.getResultado()).isEqualTo("{\"data\":[]}");
        assertThat(cache.getGeneradoPor()).isEqualTo(5L);
        assertThat(cache.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void reportCache_noArgsConstructor_creaInstanciaVacia() {
        ReportCache cache = new ReportCache();

        assertThat(cache.getId()).isNull();
        assertThat(cache.getTipo()).isNull();
        assertThat(cache.getResultado()).isNull();
        assertThat(cache.getGeneradoPor()).isNull();
    }

    @Test
    void reportCache_allArgsConstructor_inicializaTodosLosCampos() {
        LocalDateTime now = LocalDateTime.now();

        ReportCache cache = new ReportCache(1L, "DASHBOARD", null,
                "{\"kpi\":1}", 10L, now);

        assertThat(cache.getId()).isEqualTo(1L);
        assertThat(cache.getTipo()).isEqualTo("DASHBOARD");
        assertThat(cache.getGeneradoPor()).isEqualTo(10L);
    }

    @Test
    void reportCache_setters_actualizanCampos() {
        ReportCache cache = new ReportCache();

        cache.setId(3L);
        cache.setTipo("KPI_REPORT");
        cache.setFiltros("{\"userId\":2}");
        cache.setResultado("{\"total\":5}");
        cache.setGeneradoPor(7L);
        cache.setCreatedAt(LocalDateTime.now());

        assertThat(cache.getId()).isEqualTo(3L);
        assertThat(cache.getTipo()).isEqualTo("KPI_REPORT");
        assertThat(cache.getFiltros()).isEqualTo("{\"userId\":2}");
        assertThat(cache.getResultado()).isEqualTo("{\"total\":5}");
        assertThat(cache.getGeneradoPor()).isEqualTo(7L);
        assertThat(cache.getCreatedAt()).isNotNull();
    }

    @Test
    void reportCache_equals_conMismosValores_sonIguales() {
        LocalDateTime now = LocalDateTime.now();

        ReportCache c1 = ReportCache.builder().id(1L).tipo("T").filtros(null)
                .resultado("{}").generadoPor(1L).createdAt(now).build();

        ReportCache c2 = ReportCache.builder().id(1L).tipo("T").filtros(null)
                .resultado("{}").generadoPor(1L).createdAt(now).build();

        assertThat(c1).isEqualTo(c2);
    }

    @Test
    void reportCache_equals_conIdsDiferentes_noSonIguales() {
        ReportCache c1 = ReportCache.builder().id(1L).tipo("T")
                .resultado("{}").generadoPor(1L).build();

        ReportCache c2 = ReportCache.builder().id(2L).tipo("T")
                .resultado("{}").generadoPor(1L).build();

        assertThat(c1).isNotEqualTo(c2);
    }

    @Test
    void reportCache_hashCode_consistente() {
        LocalDateTime now = LocalDateTime.now();

        ReportCache c1 = ReportCache.builder().id(1L).tipo("T").filtros(null)
                .resultado("{}").generadoPor(1L).createdAt(now).build();

        ReportCache c2 = ReportCache.builder().id(1L).tipo("T").filtros(null)
                .resultado("{}").generadoPor(1L).createdAt(now).build();

        assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
    }

    @Test
    void reportCache_toString_contieneInformacion() {
        ReportCache cache = ReportCache.builder()
                .id(1L).tipo("PROJECT_REPORT")
                .resultado("{}").generadoPor(5L).build();

        String str = cache.toString();
        assertThat(str).contains("ReportCache");
        assertThat(str).contains("tipo=PROJECT_REPORT");
    }

    @Test
    void reportCache_onCreate_asignaCreatedAt() {
        ReportCache cache = new ReportCache();
        cache.onCreate();

        assertThat(cache.getCreatedAt()).isNotNull();
        assertThat(cache.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void reportCache_conFiltrosNull_esValido() {
        ReportCache cache = ReportCache.builder()
                .id(1L).tipo("DASHBOARD")
                .filtros(null).resultado("{}")
                .generadoPor(1L).build();

        assertThat(cache.getFiltros()).isNull();
    }

    @Test
    void reportCache_notEqualToNull() {
        ReportCache cache = ReportCache.builder().id(1L).tipo("T")
                .resultado("{}").generadoPor(1L).build();

        assertThat(cache).isNotEqualTo(null);
    }

    @Test
    void kpiSnapshot_notEqualToNull() {
        KpiSnapshot snapshot = KpiSnapshot.builder().id(1L).projectId(1L)
                .totalTareas(0).tareasCompletadas(0).tareasEnProgreso(0)
                .tareasPendientes(0).porcentajeAvance(BigDecimal.ZERO).build();

        assertThat(snapshot).isNotEqualTo(null);
    }
}
