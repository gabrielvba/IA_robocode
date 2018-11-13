package ia_robocode;

import static robocode.util.Utils.normalRelativeAngleDegrees;

import robocode.*;
import java.awt.Color;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * KnightMare - Implementação de um robo que utiliza lógica de Fuzzy
 */
public class KnightMare extends AdvancedRobot {
	
	//Variáveis para aplicação de lógica de Fuzzy
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
	 * Método para execução do robô, onde é inicializado o robo
	 */
	public void run() {
		
		colorfy();
		
		// Loop principal para execução do robô
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
	 * Configuração das cores do robô e de seus componentes
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
	 * onScannedRobot: Ação realizada quando o robo vê um inimigo
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
	 * Função que chama os métodos de fuzzificação, reset e geração
	 * @param evento objeto SannedRobotEvent da API do robocode que possui 
	 * vários métodos que podem ser utilizados para determinar as ações
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
	 * Método para aplicação da fuzzificação na energia do robo
	 * @param energia nível de energia atual do robo
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
	 * Método para aplicação da fuzzificação na distância do robo com inimigo
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
	 * Método que realiza a defuzificação, onde cada condição mais externa representa:
	 * 		- Agressividade Baixa;
	 * 		- Agressividade entre Baixa e Média;
	 * 		- Agressividade Média;
	 * 		- Agressividade entre Média e Alta;
	 * 		- Agressividade Alta.
	 * 
	 * @return variavel defuzificada que servirá para definir a potencia do tiro
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
	 * Gera a agressividade através do nível de energia e da distância:
	 * 
	 * 	- Energia Baixa e Distância curta, então agressividade dos tiros média;
	 *  - Energia Baixa e Distância longa, então agressividade dos tiros baixa;
	 * 	- Energia Alta e Distância curta, então agressividade dos tiros maior;
	 *  - Energia Alta e Distância longa, então agressividade dos tiros menor;
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
	 * onHitWall: Ação quando o robo bate em uma parede
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
