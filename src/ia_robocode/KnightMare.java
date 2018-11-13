package ia_robocode;

import static robocode.util.Utils.normalRelativeAngleDegrees;

import robocode.*;
import java.awt.Color;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * KnightMare - Implementa��o de um robo que utiliza l�gica de Fuzzy
 */
public class KnightMare extends AdvancedRobot {
	
	//Vari�veis para aplica��o de l�gica de Fuzzy
	private double energia = 0;
	private double energiaNivelBaixo = 0;
	private double energiaNivelAlto = 0;
    private double distanciaCurta = 0;
    private double distanciaLonga = 0;
	private double agressivadadeMedia = 0;
	private double agressividadeBaixa = 0;
	private double agressividadeAlta = 0;
	private double precisao = 0.05;
	private double rolamentoCanhao = 0;
	private double ultimoRolamentoCanhao = 0;
	/**
	 * M�todo para execu��o do rob�, onde � inicializado o robo
	 */
	public void run() {
		
		colorfy();
		
		// Loop principal para execu��o do rob�
		while(true) {
			
			// Replace the next 4 lines with any behavior you would like
			energia = this.getEnergy();
			
			if(ultimoRolamentoCanhao <= 0){
				turnLeft(10);
			} else {
				turnRight(10);
			}
		}
	}

	/**
	 * Configura��o das cores do rob� e de seus componentes
	 * 
	 */
	private void colorfy() {
		setBodyColor(Color.pink);
		setGunColor(Color.yellow);
		setRadarColor(Color.lightGray);
		setBulletColor(Color.cyan);
		setScanColor(Color.red);
	}

	/**
	 * onScannedRobot: A��o realizada quando o robo v� um inimigo
	 */
	public void onScannedRobot(ScannedRobotEvent evento) {
		this.energia = this.getEnergy();
		
		//Retorna em graus
		double rolamentoAbs = getHeading() + evento.getBearing();
		
		this.ultimoRolamentoCanhao = this.rolamentoCanhao;
		this.rolamentoCanhao = normalRelativeAngleDegrees(rolamentoAbs - getGunHeading());

		turnRight(this.rolamentoCanhao);
		
		if (Math.abs(this.rolamentoCanhao) <= 3.0) {
			if (getGunHeat() == 0) {
				this.fuzzyControle(evento);
			}
		}
		if (this.rolamentoCanhao == 0) {
			scan();
		}
	}
	
	/**
	 * 
	 * Fun��o que chama os m�todos de fuzzifica��o, reset e gera��o
	 * @param evento objeto SannedRobotEvent da API do robocode que possui 
	 * v�rios m�todos que podem ser utilizados para determinar as a��es
	 * 
	 */
	private void fuzzyControle(ScannedRobotEvent evento) {
		this.fuzzificarEnergia(this.energia);
		this.fuzzificarDistancia(evento.getDistance());
		this.resetAgressividade();
		this.gerarAgressividade();
		
		double potenciaTiro = defuzificar();
		
		fire(potenciaTiro);		
		ahead(20);
	}

	/**
	 * 
	 * M�todo para aplica��o da fuzzifica��o na energia do robo
	 * @param energia n�vel de energia atual do robo
	 */
	private void fuzzificarEnergia(double energia) {
		if (energia <= 40) {
			this.energiaNivelAlto = 0.0;
			this.energiaNivelBaixo = 1.0;
		} else if (energia < 60) {
			this.energiaNivelAlto = (energia - 40) / 20.0;
			this.energiaNivelBaixo = (60 - energia) / 20.0;
		} else {
			this.energiaNivelAlto = 1.0;
			this.energiaNivelBaixo = 0.0;
		}
	}
	
	/**
	 * M�todo para aplica��o da fuzzifica��o na dist�ncia do robo com inimigo
	 * @param distancia
	 */
	private void fuzzificarDistancia(double distancia) {
		if (distancia <= 180) {
			this.distanciaLonga = 0.0;
			this.distanciaCurta = 1.0;
		} else if (distancia < 280) {
			this.distanciaLonga = (distancia - 180) / 100;
			this.distanciaCurta = (280 - distancia) / 100;
		} else {
			this.distanciaLonga = 1;
			this.distanciaCurta = 0;
		}
	}
	
	/**
	 * M�todo que realiza a defuzifica��o, onde cada condi��o mais externa representa:
	 * 		- Agressividade Baixa;
	 * 		- Agressividade entre Baixa e M�dia;
	 * 		- Agressividade M�dia;
	 * 		- Agressividade entre M�dia e Alta;
	 * 		- Agressividade Alta.
	 * 
	 * @return variavel defuzificada que servir� para definir a potencia do tiro
	 */
	private double defuzificar() {
		double variavelSaida = 0.0;
		double controle = 0.0;
		
		for (double i = 0.0; i <= 3.0; i+= precisao) {
			
			if (i >= 0.0 && i < 1.0) {
				if (this.agressividadeBaixa != 0) {
					variavelSaida += this.agressividadeBaixa * i;
					controle++;
				}
			} else if (i <= 1.5) {
				if (this.agressividadeBaixa > this.agressivadadeMedia) {
					if (this.agressividadeBaixa > 0) {
						variavelSaida += this.agressividadeBaixa * i;
						controle++;
					}
				} else {
					if (this.agressivadadeMedia > 0) {
						variavelSaida += this.agressivadadeMedia * i;
						controle++;
					}
				}
			} else if (i < 2.0) {
				if (this.agressivadadeMedia > 0) {
					variavelSaida += this.agressivadadeMedia * i;
					controle++;
				}
			} else if (i <= 2.5) {
				if (this.agressivadadeMedia > this.agressividadeAlta) {
					if (this.agressivadadeMedia > 0) {
						variavelSaida += this.agressivadadeMedia * i;
						controle++;
					}
				} else {
					if (this.agressividadeAlta > 0) {
						variavelSaida += this.agressividadeAlta * i;
						controle++;
					}
				}
			} else if (i > 2.5) {
				if (this.agressividadeAlta > 0) {
					variavelSaida += this.agressividadeAlta * i;
					controle++;
				}
			}		
		}
		
		variavelSaida /= controle;
		
		return variavelSaida;
	}

	/**
	 * Reseta a agressividade do robo para 0
	 */
	private void resetAgressividade() {
		this.agressividadeBaixa = 0;
		this.agressivadadeMedia = 0;
		this.agressividadeAlta = 0;
	}

	/**
	 * 
	 * Gera a agressividade atrav�s do n�vel de energia e da dist�ncia:
	 * 
	 * 	- Energia Baixa e Dist�ncia curta, ent�o agressividade dos tiros m�dia;
	 *  - Energia Baixa e Dist�ncia longa, ent�o agressividade dos tiros baixa;
	 * 	- Energia Alta e Dist�ncia curta, ent�o agressividade dos tiros maior;
	 *  - Energia Alta e Dist�ncia longa, ent�o agressividade dos tiros menor;
	 * 
	 */
	private void gerarAgressividade() {

		if (this.energiaNivelBaixo > 0) {
			if (this.distanciaCurta > 0) {
				this.agressivadadeMedia += this.energiaNivelBaixo * this.distanciaCurta;
			}

			if (this.distanciaLonga > 0) {
				this.agressividadeBaixa += this.energiaNivelBaixo * this.distanciaLonga;
			}
		}
		
		if (this.energiaNivelAlto > 0) {
			if (this.distanciaCurta > 0) {
				this.agressividadeAlta += this.agressividadeAlta * this.distanciaCurta;
			}

			if (this.distanciaLonga > 0) {
				this.agressivadadeMedia += this.agressividadeAlta * this.distanciaLonga;
			}
		}
	}
	
	/**
	 * onHitWall: A��o quando o robo bate em uma parede
	 */
	public void onHitWall(HitWallEvent e) {
		 back(400);
		 turnRight(90);
		 ahead(90);
	}
	
	public void onHitRobot(HitRobotEvent e) {
		back(30);
	}

}
