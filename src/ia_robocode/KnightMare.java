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
	private double hostilidadeMedia = 0;
	private double hostilidadeBaixa = 0;
	private double hostilidadeAlta = 0;
	private double precisao = 0.01;
	private double rolamentoCanhao = 0;
	private double ultimoRolamentoCanhao = 0;
	/**
	 * Método para execução do robô, onde é inicializado o robo
	 */
	public void run() {
		
		roboColor();
		this.energia = this.getEnergy();

		// Loop principal para execução do robô
		while(true) {
			energia = this.getEnergy();

			// Replace the next 4 lines with any behavior you would like
			
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
	private void roboColor() {
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
		} else {
			turnRight(rolamentoCanhao);
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
		this.resetHostilidade();
		this.criaHostilidade();
		
		double potenciaTiro = defuzificar();
		
		System.out.println("Potência: " + potenciaTiro);
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
	 * 		- hostilidade Baixa;
	 * 		- hostilidade entre Baixa e Média;
	 * 		- hostilidade Média;
	 * 		- hostilidade entre Média e Alta;
	 * 		- hostilidade Alta.
	 * 
	 * @return variavel defuzificada que servirá para definir a potencia do tiro
	 */
	private double defuzificar() {
		double variavelSaida = 0.0;
		double controle = 0.0;
		
		for (double i = 0.0; i <= 3.0; i+= precisao) {
			
			if (i >= 0.0 && i < 1.0) {
				if (this.hostilidadeBaixa != 0) {
					variavelSaida += this.hostilidadeBaixa * i;
					controle += 1;
				}
			} else if (i <= 1.5) {
				if (this.hostilidadeBaixa > this.hostilidadeMedia) {
					if (this.hostilidadeBaixa > 0) {
						variavelSaida += this.hostilidadeBaixa * i;
						controle += 1;
					}
				} else {
					if (this.hostilidadeMedia > 0) {
						variavelSaida += this.hostilidadeMedia * i;
						controle += 1;
					}
				}
			} else if (i < 2.0) {
				if (this.hostilidadeMedia > 0) {
					variavelSaida += this.hostilidadeMedia * i;
					controle += 1;
				}
			} else if (i <= 2.5) {
				if (this.hostilidadeMedia > this.hostilidadeAlta) {
					if (this.hostilidadeMedia > 0) {
						variavelSaida += this.hostilidadeMedia * i;
						controle += 1;
					}
				} else {
					if (this.hostilidadeAlta > 0) {
						variavelSaida += this.hostilidadeAlta * i;
						controle += 1;
					}
				}
			} else if (i > 2.5) {
				if (this.hostilidadeAlta > 0) {
					variavelSaida += this.hostilidadeAlta * i;
					controle += 1;
				}
			}		
		}
		
		variavelSaida /= controle;
		
		return variavelSaida;
	}

	/**
	 * Reseta a hostilidade do robo para 0
	 */
	private void resetHostilidade() {
		this.hostilidadeBaixa = 0;
		this.hostilidadeMedia = 0;
		this.hostilidadeAlta = 0;
	}

	/**
	 * 
	 * Gera a hostilidade através do nível de energia e da distância:
	 * 
	 * 	- Energia Baixa e Distância curta, então hostilidade dos tiros média;
	 *  - Energia Baixa e Distância longa, então hostilidade dos tiros baixa;
	 * 	- Energia Alta e Distância curta, então hostilidade dos tiros maior;
	 *  - Energia Alta e Distância longa, então hostilidade dos tiros menor;
	 * 
	 */
	private void criaHostilidade() {

		if (this.energiaNivelBaixo > 0) {
			if (this.distanciaCurta > 0) {
				this.hostilidadeMedia += this.energiaNivelBaixo * this.distanciaCurta;
			}

			if (this.distanciaLonga > 0) {
				this.hostilidadeBaixa += this.energiaNivelBaixo * this.distanciaLonga;
			}
		}
		
		if (this.energiaNivelAlto > 0) {
			if (this.distanciaCurta > 0) {
				this.hostilidadeAlta += this.hostilidadeAlta * this.distanciaCurta;
			}

			if (this.distanciaLonga > 0) {
				this.hostilidadeMedia += this.hostilidadeAlta * this.distanciaLonga;
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
