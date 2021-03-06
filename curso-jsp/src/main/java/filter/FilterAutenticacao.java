package filter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import connection.SingleConnectionBanco;
import dao.DAOVersionadorBanco;


@WebFilter(urlPatterns = { "/principal/*" }) // intercepta todas as requisi??es do projeto ou mapeamento
public class FilterAutenticacao extends HttpFilter {

	private static final long serialVersionUID = 1L;
	
	private static Connection connection;

	public FilterAutenticacao() {
		super();
	}

	public void destroy() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try {
			//Pegando usuario logado
			HttpServletRequest req = (HttpServletRequest) request;//ServletRequest -> HttpServletRequest
			HttpSession session = req.getSession();
			String usuarioLogado = (String) session.getAttribute("usuario");
			
			String urlParaAutenticar = req.getServletPath();// url que esta sendo acessada
			
			//validar se esta logado senao redireciona para a tela de login
			//sendo que n?o est? logado(a l?gica desse if a pessoa tentando acessar uma url diferente n?o pode...), tentando acessar qualquer parte do sistema sem estar logado, n?o pode...
			if (usuarioLogado == null && !urlParaAutenticar.equalsIgnoreCase("/principal/ServletLogin")) { // nao logado
				RequestDispatcher redireciona = request.getRequestDispatcher("/index.jsp?url="+urlParaAutenticar);//seta o parametro url com o valor da url que o usuario queria acessar
				request.setAttribute("msg", "Por favor realize o login!");
				redireciona.forward(request, response);
				return; // para a execu??o e redireciona para o login;quebrando o do filter que s? acabava com o chain creio
			} else {// logado
				chain.doFilter(request, response);
			}
			connection.commit();
		} catch (Exception e) {
			e.printStackTrace();
			
			RequestDispatcher redirecionar = request.getRequestDispatcher("erro.jsp");
			request.setAttribute("msg", e.getMessage());
			redirecionar.forward(request, response);
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	public void init(FilterConfig fConfig) throws ServletException {
		connection = SingleConnectionBanco.getConnection();
		/*
		DAOVersionadorBanco daoVersionadorBanco=new DAOVersionadorBanco();
		
		String caminhoPastaSQL=fConfig.getServletContext().getRealPath("versionadorbancosql")+File.separator;
		
		File[] filesSql=new File(caminhoPastaSQL).listFiles();
		try {
			for (File file : filesSql) {
				boolean arquivoJaRodado=daoVersionadorBanco.arquivoSqlRodado(file.getName());
				if(!arquivoJaRodado) {
					FileInputStream entradaArquivo=new FileInputStream(file);
					Scanner lerArquivo=new Scanner(entradaArquivo,"UTF-8");
					StringBuilder sql=new StringBuilder();
					while(lerArquivo.hasNext()) {
						sql.append(lerArquivo.nextLine());
						sql.append("\n");
					}
					connection.prepareStatement(sql.toString()).execute();
					daoVersionadorBanco.gravaArquivoSqlRodado(file.getName());
					
					connection.commit();
					lerArquivo.close();
				}
			}
		}catch (Exception e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		*/
	}

}
