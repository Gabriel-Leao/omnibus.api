package br.com.leao.gabriel.omnibus.adapter.in.web.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class TraceIdFilterTest {

  private final TraceIdFilter filter = new TraceIdFilter();

  @Test
  void shouldAddTraceIdHeaderAndClearMdcAfterRequest() throws Exception {
    var response = new MockHttpServletResponse();

    filter.doFilter(
        new MockHttpServletRequest(),
        response,
        (request, ignoredResponse) ->
            assertThat(MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY)).isNotBlank());

    assertThat(response.getHeader(TraceIdFilter.TRACE_ID_HEADER)).isNotBlank();
    assertThat(MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY)).isNull();
  }
}
