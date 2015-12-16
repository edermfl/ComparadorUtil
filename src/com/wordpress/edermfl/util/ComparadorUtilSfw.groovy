/**
 *
 */
package br.com.synchro.sfw.application.shared

import br.com.synchro.framework.ha.integration.api.IAuditavel
import br.com.synchro.sfw.domain.shared.IDadoComAtributosControle
import br.com.synchro.sfw.infra.util.IVersionable
import br.com.synchro.sfw.infra.util.ReflectionUtilSfw
import br.com.synchro.td.interfaces.IDisponibilizarResultadoValidacao
import br.com.synchro.td.interfaces.IEstruturaHierarquica
import br.com.synchro.td.interfaces.IResumoCriticas
import org.apache.commons.beanutils.PropertyUtils
import org.apache.commons.lang.StringUtils

/**
 * Utilit�rio de compara��o de objetos
 * @author eder.leite
 * @version 1.0 06/12/2012
 */
class ComparadorUtilSfw {

    /**
     * Classe respons�vel por comparar o objetoOriginal com o objetoModificado e instancia um objeto OperacoesComparacao para possibilitar as compara��es.
     * @author eder.leite
     * @version 1.0 07/12/2012
     */
    class ComparadorDeObjeto {

        private def objetoOriginal
        private def camposAComparar = []

        def ComparadorDeObjeto(pObjeto) {
            objetoOriginal = pObjeto
        }

        /**
         * Com o objeto modificado. Aqui � aceito um outro objeto do mesmo tipo ou um Map com os atributos de nome que o obejtoOriginal.
         * Obs.: Caso no mapa exista um atributo que n�o exista na entidade, esse atributo � ignorado, pois n�o � base de compara��o.
         * @param pObjetoModificado
         */
        public OperacoesComparacao comModificado(pObjetoModificado) {
            validaObjeto(pObjetoModificado)
            final Map mapDadosModificados = obterMapaDadosObjetoModificado(pObjetoModificado)
            final Map mapComDiferencas = [:]
            final Map mapComIgualdades = [:]
            mapDadosModificados.each { chave, valorModificado ->
                if (objetoOriginal.respondsTo("get${chave.capitalize()}")) {
                    if (objetoOriginal."${chave}" != valorModificado) {
                        mapComDiferencas.put(chave, valorModificado)
                    } else {
                        mapComIgualdades.put(chave, valorModificado)
                    }
                }
            }

            return new OperacoesComparacao(mapComDiferencas, mapComIgualdades)
        }

        /**
         * @param pString
         */
        public ComparadorDeObjeto peloCampos(... pListaCampos) {
            camposAComparar.addAll(pListaCampos)
            return this
        }

        /**
         * Converte o objetoModificado em um Mapa
         */
        private Map obterMapaDadosObjetoModificado(pObjetoModificado) {
            Map retorno = null
            if (pObjetoModificado instanceof Map) {
                retorno = pObjetoModificado
            } else {
                retorno = PropertyUtils.describe(pObjetoModificado)
            }
            if (camposAComparar) {
                Map subRetorno = [:]
                camposAComparar.each { campo ->
                    subRetorno.put(campo, retorno.get(campo))
                }
                retorno = subRetorno
            }
            return retorno;
        }

        /**
         * Verifica se o objetoModificado � de mesmo tipo do objetoOriginal ou � um Map, caso contr�rio lan�a IllegalArgumentException.
         * @throws IllegalArgumentException
         */
        private void validaObjeto(pObjetoModificado) {
            if (!pObjetoModificado.getClass().equals(objetoOriginal.getClass()) &&
                    !(pObjetoModificado instanceof Map)) {
                throw new IllegalArgumentException("Objeto Modificado deve ser do mesmo tipo de ${objetoOriginal.class.name} ou instancia de ${Map.class.name}");
            }
        }
    }

    /**
     * Classe respons�vel por disponibilizar as opera��es poss�veis com as diferen�as e igualdades
     * @author eder.leite
     * @version 1.0 07/12/2012
     */
    class OperacoesComparacao {

        private Map mapaComDiferencas
        private Map mapaComIgualdades

        def OperacoesComparacao(Map pMapaComDiferencas, Map pMapaComIgualdades) {
            mapaComDiferencas = pMapaComDiferencas
            mapaComIgualdades = pMapaComIgualdades
        }

        /**
         * @return Mapa contendo as diferen�as entre o objetoOriginal e o modificado (Chave: nome do atributo, Valor: valor do atributo)
         */
        public Map<String, Object> obtendoMapaComDiferencas() {
            return mapaComDiferencas;
        }

        /**
         * @return Mapa contendo as diferen�as entre o objetoOriginal e o modificado (Chave: nome do atributo, Valor: valor do atributo)
         */
        public Map<String, Object> obtendoMapaComIgualdades() {
            return mapaComIgualdades;
        }

        /**
         * Os atributos (dhInclusao, dhAlteracao, qtdCriticas, qtdCriticasFilhos e erros) e todos atributos
         * que possuem getter/setter das interfaces ( IResumoCriticas, IDadoComAtributosControle, IAuditavel,
         * IDisponibilizarResultadoValidacao, IEstruturaHierarquica, IVersionable) dos mapas com diferen�as e igualdades:</br>
         * dhInclusao </br>
         * dhAlteracao </br>
         * qtdCriticas </br>
         * qtdCriticasFilhos </br>
         * erros </br>
         */
        public OperacoesComparacao removendoAtributosDeControle() {
            Set atributosDeControle = [
                    "dhInclusao",
                    "dhAlteracao",
                    "qtdCriticas",
                    "qtdCriticasFilhos",
                    "erros"
            ]
            def listaInterfacesQueDevoRemoverAtributos = [
                    IResumoCriticas.class,
                    IDadoComAtributosControle.class,
                    IAuditavel.class,
                    IDisponibilizarResultadoValidacao.class,
                    IEstruturaHierarquica.class,
                    IVersionable.class
            ]
            atributosDeControle.addAll extrairAtributosDasInterfaces(listaInterfacesQueDevoRemoverAtributos)
            return removendoOsAtributos(atributosDeControle.toArray());
        }

        /**
         * Remove os atributos informados dos mapas de atributos com diferen�as e igualdades
         * @param pLista de atributos
         */
        public OperacoesComparacao removendoOsAtributos(... pLista) {
            pLista.each { atributo ->
                mapaComDiferencas.remove(atributo);
                mapaComIgualdades.remove(atributo);
            }
            return this;
        }

        /**
         * Extrai atributos getters/setters das interfaces informadas
         * @param listaInterfacesQueDevoRemoverAtributos
         * @return Set de nomes atributos
         */
        private Set extrairAtributosDasInterfaces(listaInterfacesQueDevoRemoverAtributos) {
            Set retorno = []
            listaInterfacesQueDevoRemoverAtributos.each {
                def metodos = ReflectionUtilSfw.obterGettersAndSetters(it)
                retorno.addAll metodos.collect {
                    StringUtils.uncapitalize(it.name.replaceAll('get|set', ''))
                }
            }
            return retorno
        }
    }

    /**
     * Compara o objeto informado trantando esse objeto com o original.
     * @param pObjeto original
     */
    public ComparadorDeObjeto compararObjetoOriginal(pObjeto) throws IllegalArgumentException {
        if (!pObjeto.class.name.startsWith("br.com.synchro")) {
            throw new IllegalArgumentException("Objeto estar contido na package br.com.synchro.");
        }
        return new ComparadorDeObjeto(pObjeto)
    }
}
