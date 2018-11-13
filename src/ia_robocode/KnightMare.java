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
	private double hostilidadeMedia = 0;
	private double hostilidadeBaixa = 0;
	private double hostilidadeAlta = 0;
	private double precisao = 0.01;
	private double rolamentoCanhao = 0;
	private double ultimoRolamentoCanhao = 0;
	/**
	 * M�todo para execu��o do rob�, onde � inicializado o robo
	 */
	public void run() {
		
		roboColor();
		this.energia = this.getEnergy();

		// Loop principal para execu��o do rob�
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
	 * Configura��o das cores do rob� e de seus componentes
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
		} else {
			turnRight(rolamentoCanhao);
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
		this.resetHostilidade();
		this.criaHostilidade();
		
		double potenciaTiro = defuzificar();
		
		System.out.println("Pot�ncia: " + potenciaTiro);
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
	 * 		- hostilidade Baixa;
	 * 		- hostilidade entre Baixa e M�dia;
	 * 		- hostilidade M�dia;
	 * 		- hostilidade entre M�dia e Alta;
	 * 		- hostilidade Alta.
	 * 
	 * @return variavel defuzificada que servir� para definir a potencia do tiro
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
	 * Gera a hostilidade atrav�s do n�vel de energia e da dist�ncia:
	 * 
	 * 	- Energia Baixa e Dist�ncia curta, ent�o hostilidade dos tiros m�dia;
	 *  - Energia Baixa e Dist�ncia longa, ent�o hostilidade dos tiros baixa;
	 * 	- Energia Alta e Dist�ncia curta, ent�o hostilidade dos tiros maior;
	 *  - Energia Alta e Dist�ncia longa, ent�o hostilidade dos tiros menor;
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
