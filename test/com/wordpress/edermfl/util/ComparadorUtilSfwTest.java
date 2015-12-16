/**
 *
 */
package br.com.synchro.sfw.application.shared;

import br.com.synchro.cor.cad.domain.model.mercadoria.Mercadoria;
import br.com.synchro.cor.cad.domain.model.servico.Servico;
import br.com.synchro.sfw.domain.shared.AtributosOrigem;
import br.com.synchro.sfw.infra.util.ReflectionUtilSfw;
import br.com.synchro.sfw.util.PersistenciaUtilSfw;
import br.com.synchro.td.interfaces.ResumoCriticas;
import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

/**
 * @author eder.leite
 * @version 1.0 06/12/2012
 */
public class ComparadorUtilSfwTest {

    private Map<String, Object> mapaAlteracaoMercadoria;

    private Mercadoria mercadoriaModificada = null;

    private Mercadoria mercadoriaOriginal = null;

    private PersistenciaUtilSfw util = PersistenciaUtilSfw.getInstance();

    /**
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
	// Cria uma nova mercadoriaOriginal setando id
	mercadoriaOriginal = util.criarEPopular(Mercadoria.class);
	mercadoriaOriginal.setId("idMerc001");
	mercadoriaOriginal.setResumoCriticas(new ResumoCriticas());

	// cria copia de mercadoria e altera campos
	mercadoriaModificada = (Mercadoria) ReflectionUtilSfw.criarCopiaObjetoOriginal(mercadoriaOriginal);
	mercadoriaModificada.setChvIntegracao("chvIntegracao_ApplicationUtilSfwTest");
	mercadoriaModificada.setCodigo("merc123");

	// cria mapa de através do objeto modificado
	mapaAlteracaoMercadoria = new HashMap<String, Object>();
	mapaAlteracaoMercadoria.put("id", mercadoriaModificada.getId());
	mapaAlteracaoMercadoria.put("chvIntegracao", mercadoriaModificada.getChvIntegracao());
	mapaAlteracaoMercadoria.put("codigo", mercadoriaModificada.getCodigo());
    }

    /**
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void test_Objeto_ilegal() throws Exception {
	final List<Integer> lista = Arrays.asList(1, 2);
	new ComparadorUtilSfw().compararObjetoOriginal(lista);
    }

    /**
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void test_comparaObjetoDeTiposDiferentes() throws Exception {
	final Servico servico = util.criarEPopular(Servico.class);
	new ComparadorUtilSfw().compararObjetoOriginal(mercadoriaOriginal).comModificado(servico);
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_comparaObjetoOriginalComMapaDeModificacaoObtendoDiferenca() throws Exception {
	// retorna mapa com conteúdo solicitado.
	final Map<String, Object> mapaRetorno = new ComparadorUtilSfw().compararObjetoOriginal(mercadoriaOriginal)
			.comModificado(mapaAlteracaoMercadoria).obtendoMapaComDiferencas();
	Assert.assertEquals("Quantidade de diferenças derevia ser 2.", 2, mapaRetorno.size());
	Assert.assertTrue("Deveria retornar chvIntegracao e codigo como diferenças",
			verificaIguadade("chvIntegracao,codigo", mapaRetorno.keySet()));
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_comparaObjetoOriginalComMapaDeModificacaoObtendoIgualdade() throws Exception {
	// retorna mapa com conteúdo solicitado.
	final Map<String, Object> mapaRetorno = new ComparadorUtilSfw().compararObjetoOriginal(mercadoriaOriginal)
			.comModificado(mapaAlteracaoMercadoria).obtendoMapaComIgualdades();
	Assert.assertEquals("Quantidade de igualdades derevia ser 1.", 1, mapaRetorno.size());
	Assert.assertTrue("Deveria retornar id como diferenças", verificaIguadade("id", mapaRetorno.keySet()));
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_comparaObjetoOriginalComMapaDeModificacaoRemovendoAtributosDeControleObtendoDiferenca() throws Exception {
	mapaAlteracaoMercadoria.put("controle", "Teste");
	// retorna mapa com conteúdo solicitado.
	final Map<String, Object> mapaRetorno = new ComparadorUtilSfw().compararObjetoOriginal(mercadoriaOriginal)
			.comModificado(mapaAlteracaoMercadoria).removendoAtributosDeControle().obtendoMapaComDiferencas();
	Assert.assertEquals("Quantidade de diferenças derevia ser 2.", 2, mapaRetorno.size());
	Assert.assertFalse("Não deveria retornar controle como diferença pois foi removido.",
			mapaRetorno.containsKey("controle"));
	Assert.assertTrue("Deveria retornar chvIntegracao e codigo como diferenças",
			verificaIguadade("chvIntegracao,codigo", mapaRetorno.keySet()));
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_comparaObjetoOriginalComMapaDeModificacaoRemovendoAtributosDeControleObtendoIgualdade() throws Exception {
	// Adiciona novas propriedades iguais ao original
	mapaAlteracaoMercadoria.put("controle", mercadoriaOriginal.getControle());

	// retorna mapa com conteúdo solicitado.
	final Map<String, Object> mapaRetorno = new ComparadorUtilSfw().compararObjetoOriginal(mercadoriaOriginal)
			.comModificado(mapaAlteracaoMercadoria).removendoAtributosDeControle().obtendoMapaComIgualdades();
	Assert.assertEquals("Quantidade de igualdades derevia ser 1.", 1, mapaRetorno.size());
	Assert.assertFalse("Não deveria retornar controle como igualdades pois foi removido.",
			mapaRetorno.containsKey("controle"));
	Assert.assertTrue("Deveria retornar id como igualdades", verificaIguadade("id", mapaRetorno.keySet()));
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_comparaObjetoOriginalComMapaDeModificacaoRemovendoAtributosInformadosObtendoDiferenca() throws Exception {
	mapaAlteracaoMercadoria.put("controle", "Teste");
	mapaAlteracaoMercadoria.put("erros", "Teste");

	// retorna mapa com conteúdo solicitado.
	final Map<String, Object> mapaRetorno = new ComparadorUtilSfw().compararObjetoOriginal(mercadoriaOriginal)
			.comModificado(mapaAlteracaoMercadoria).removendoOsAtributos("controle", "erros")
			.obtendoMapaComDiferencas();
	Assert.assertEquals("Quantidade de diferenças derevia ser 2.", 2, mapaRetorno.size());
	Assert.assertFalse("Não deveria retornar controle como diferença pois foi removido.",
			mapaRetorno.containsKey("controle"));
	Assert.assertFalse("Não deveria retornar controle como diferença pois foi removido.", mapaRetorno.containsKey("erros"));
	Assert.assertTrue("Deveria retornar chvIntegracao e codigo como diferenças",
			verificaIguadade("chvIntegracao,codigo", mapaRetorno.keySet()));
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_comparaObjetoOriginalComMapaDeModificacaoRemovendoAtributosInformadosObtendoIgualdade() throws Exception {
	// Adiciona novas propriedades iguais ao original
	mapaAlteracaoMercadoria.put("controle", mercadoriaOriginal.getControle());
	mapaAlteracaoMercadoria.put("erros", mercadoriaOriginal.erros());

	// retorna mapa com conteúdo solicitado.
	final Map<String, Object> mapaRetorno = new ComparadorUtilSfw().compararObjetoOriginal(mercadoriaOriginal)
			.comModificado(mapaAlteracaoMercadoria).removendoOsAtributos("controle", "erros")
			.obtendoMapaComIgualdades();
	Assert.assertEquals("Quantidade de igualdades derevia ser 1.", 1, mapaRetorno.size());
	Assert.assertFalse("Não deveria retornar controle como igualdades pois foi removido.",
			mapaRetorno.containsKey("controle"));
	Assert.assertFalse("Não deveria retornar erros como igualdades pois foi removido.", mapaRetorno.containsKey("controle"));
	Assert.assertTrue("Deveria retornar id como igualdades", verificaIguadade("id", mapaRetorno.keySet()));
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_comparaObjetoOriginalComObjetoDeModificacaoObtendoDiferenca() throws Exception {
	// retorna mapa com conteúdo solicitado.
	final Map<String, Object> mapaRetorno = new ComparadorUtilSfw().compararObjetoOriginal(mercadoriaOriginal)
			.comModificado(mercadoriaModificada).obtendoMapaComDiferencas();
	Assert.assertEquals("Quantidade de diferenças derevia ser 3.", 3, mapaRetorno.size());
	Assert.assertTrue("Deveria retornar identificacaoFuncionalDeReferencia, chvIntegracao e codigo como diferenças",
			verificaIguadade("identificacaoFuncionalDeReferencia,chvIntegracao,codigo", mapaRetorno.keySet()));
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_comparaObjetoOriginalComObjetoDeModificacaoObtendoIgualdade() throws Exception {
	// retorna mapa com conteúdo solicitado.
	final Map<String, Object> mapaRetorno = new ComparadorUtilSfw().compararObjetoOriginal(mercadoriaOriginal)
			.comModificado(mercadoriaModificada).obtendoMapaComIgualdades();
	Assert.assertEquals("Quantidade de igualdades derevia ser 9.", 9, mapaRetorno.size());
	Assert.assertTrue(
			"Deveria retornar auditavelProps, origem, mercadoriaVigencias, class, controle, resumoCriticas, objVersion, pai e id como igualdades",
			verificaIguadade(
					"auditavelProps,controle,origem,mercadoriaVigencias,class,resumoCriticas,objVersion,pai,id",
					mapaRetorno.keySet()));
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_comparaObjetoOriginalComObjetoDeModificacaoRemovendoAtributosDeControleEAtributosInformadosObtendoDiferenca()
		    throws Exception {
	mercadoriaModificada.setOrigem(new AtributosOrigem());

	// retorna mapa com conteúdo solicitado.
	final Map<String, Object> mapaRetorno = new ComparadorUtilSfw().compararObjetoOriginal(mercadoriaOriginal)
			.comModificado(mercadoriaModificada).removendoAtributosDeControle()
			.removendoOsAtributos("origem", "identificacaoFuncionalDeReferencia").obtendoMapaComDiferencas();
	Assert.assertEquals("Quantidade de diferenças derevia ser 2.", 2, mapaRetorno.size());
	Assert.assertFalse("Não deveria retornar controle como diferença pois foi removido.",
			mapaRetorno.containsKey("controle"));
	Assert.assertFalse("Não deveria retornar origem como diferença pois foi removido.", mapaRetorno.containsKey("origem"));
	Assert.assertTrue("Deveria retornar chvIntegracao e codigo como diferenças",
			verificaIguadade("chvIntegracao,codigo", mapaRetorno.keySet()));
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_comparaObjetoOriginalComObjetoDeModificacaoRemovendoAtributosDeControleObtendoDiferenca() throws Exception {
	mercadoriaModificada.setControle(null);

	// retorna mapa com conteúdo solicitado.
	final Map<String, Object> mapaRetorno = new ComparadorUtilSfw().compararObjetoOriginal(mercadoriaOriginal)
			.comModificado(mercadoriaModificada).removendoAtributosDeControle().obtendoMapaComDiferencas();
	Assert.assertEquals("Quantidade de diferenças derevia ser 3.", 3, mapaRetorno.size());
	Assert.assertFalse("Não deveria retornar controle como diferença pois foi removido.",
			mapaRetorno.containsKey("controle"));
	Assert.assertTrue("Deveria retornar identificacaoFuncionalDeReferencia, chvIntegracao e codigo como diferenças",
			verificaIguadade("identificacaoFuncionalDeReferencia,chvIntegracao,codigo", mapaRetorno.keySet()));
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_comparaObjetoOriginalComObjetoDeModificacaoRemovendoAtributosDeControleObtendoIgualdade() throws Exception {
	// seta propriedades iguais ao original
	mercadoriaModificada.setControle(mercadoriaOriginal.getControle());

	// retorna mapa com conteúdo solicitado.
	final Map<String, Object> mapaRetorno = new ComparadorUtilSfw().compararObjetoOriginal(mercadoriaOriginal)
			.comModificado(mercadoriaModificada).removendoAtributosDeControle().obtendoMapaComIgualdades();
	Assert.assertEquals("Quantidade de igualdades derevia ser 4.", 4, mapaRetorno.size());
	Assert.assertFalse("Não deveria retornar controle como igualdades pois foi removido.",
			mapaRetorno.containsKey("controle"));
	Assert.assertTrue("Deveria retornar origem, mercadoriaVigencias, class e id como igualdades",
			verificaIguadade("origem,mercadoriaVigencias,class,id", mapaRetorno.keySet()));
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_comparaObjetoOriginalComObjetoDeModificacaoRemovendoAtributosInformadosObtendoDiferenca() throws Exception {
	mercadoriaModificada.setControle(null);
	mercadoriaModificada.setOrigem(new AtributosOrigem());

	// retorna mapa com conteúdo solicitado.
	final Map<String, Object> mapaRetorno = new ComparadorUtilSfw().compararObjetoOriginal(mercadoriaOriginal)
			.comModificado(mercadoriaModificada).removendoOsAtributos("controle", "origem")
			.obtendoMapaComDiferencas();
	Assert.assertEquals("Quantidade de diferenças derevia ser 3.", 3, mapaRetorno.size());
	Assert.assertFalse("Não deveria retornar controle como diferença pois foi removido.",
			mapaRetorno.containsKey("controle"));
	Assert.assertFalse("Não deveria retornar origem como diferença pois foi removido.", mapaRetorno.containsKey("origem"));
	Assert.assertTrue("Deveria retornar identificacaoFuncionalDeReferencia, chvIntegracao e codigo como diferenças",
			verificaIguadade("identificacaoFuncionalDeReferencia,chvIntegracao,codigo", mapaRetorno.keySet()));
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_comparaObjetoOriginalComObjetoDeModificacaoRemovendoAtributosInformadosObtendoIgualdade() throws Exception {
	// seta propriedades iguais ao original
	mercadoriaModificada.setControle(mercadoriaOriginal.getControle());
	mercadoriaModificada.setOrigem(mercadoriaOriginal.getOrigem());

	// retorna mapa com conteúdo solicitado.
	final Map<String, Object> mapaRetorno = new ComparadorUtilSfw().compararObjetoOriginal(mercadoriaOriginal)
			.comModificado(mercadoriaModificada).removendoOsAtributos("controle", "origem")
			.obtendoMapaComIgualdades();
	Assert.assertEquals("Quantidade de igualdades derevia ser 7.", 7, mapaRetorno.size());
	Assert.assertFalse("Não deveria retornar controle como igualdades pois foi removido.",
			mapaRetorno.containsKey("controle"));
	Assert.assertFalse("Não deveria retornar origem como igualdades pois foi removido.", mapaRetorno.containsKey("controle"));
	Assert.assertTrue(
			"Deveria retornar auditavelProps, mercadoriaVigencias, class, resumoCriticas, objVersion, pai e id como igualdades",
			verificaIguadade("auditavelProps,mercadoriaVigencias,class,resumoCriticas,objVersion,pai,id",
					mapaRetorno.keySet()));

    }

    /**
     * @throws Exception
     */
    @Test
    public void test_comparaObjetoOriginalPeloCamposInformadosComMapaDeModificacaoObtendoDiferenca() throws Exception {
	// retorna mapa com conteúdo solicitado.
	final Map<String, Object> mapaRetorno = new ComparadorUtilSfw().compararObjetoOriginal(mercadoriaOriginal)
			.peloCampos("id", "codigo").comModificado(mapaAlteracaoMercadoria).obtendoMapaComDiferencas();
	Assert.assertEquals("Quantidade de diferenças derevia ser 1.", 1, mapaRetorno.size());
	Assert.assertTrue("Deveria retornar codigo como diferenças", verificaIguadade("codigo", mapaRetorno.keySet()));
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_comparaObjetoOriginalPeloCamposInformadosComMapaDeModificacaoObtendoIgualdade() throws Exception {
	// retorna mapa com conteúdo solicitado.
	final Map<String, Object> mapaRetorno = new ComparadorUtilSfw().compararObjetoOriginal(mercadoriaOriginal)
			.peloCampos("id", "codigo").comModificado(mapaAlteracaoMercadoria).obtendoMapaComIgualdades();
	Assert.assertEquals("Quantidade de igualdades derevia ser 1.", 1, mapaRetorno.size());
	Assert.assertTrue("Deveria retornar id como igualdades", verificaIguadade("id", mapaRetorno.keySet()));
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_comparaObjetoOriginalPeloCamposInformadosComObjetoDeModificacaoObtendoDiferenca() throws Exception {
	// retorna mapa com conteúdo solicitado.
	final Map<String, Object> mapaRetorno = new ComparadorUtilSfw().compararObjetoOriginal(mercadoriaOriginal)
			.peloCampos("id", "codigo").comModificado(mercadoriaModificada).obtendoMapaComDiferencas();
	Assert.assertEquals("Quantidade de diferenças derevia ser 1.", 1, mapaRetorno.size());
	Assert.assertTrue("Deveria retornar codigo como diferenças", verificaIguadade("codigo", mapaRetorno.keySet()));
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_comparaObjetoOriginalPeloCamposInformadosComObjetoDeModificacaoObtendoIgualdade() throws Exception {
	// retorna mapa com conteúdo solicitado.
	final Map<String, Object> mapaRetorno = new ComparadorUtilSfw().compararObjetoOriginal(mercadoriaOriginal)
			.peloCampos("id", "codigo").comModificado(mercadoriaModificada).obtendoMapaComIgualdades();
	Assert.assertEquals("Quantidade de igualdades derevia ser 1.", 1, mapaRetorno.size());
	Assert.assertTrue("Deveria retornar id como igualdades", verificaIguadade("id", mapaRetorno.keySet()));
    }

    private boolean verificaIguadade(final String pEsperado, final Collection<String> pListaRetorno) {
	String esperadoAux = pEsperado;
	for (final String retorno : pListaRetorno) {
	    if (StringUtils.contains(esperadoAux, retorno)) {
		esperadoAux = StringUtils.remove(esperadoAux, retorno);
	    } else {
		return false;
	    }
	}
	if (esperadoAux.replaceAll(",", "").trim().length() > 0) {
	    return false;
	}
	return true;

    }
}