package com.newton.dolensi.sabotabot;

import com.bueno.spi.model.*;
import com.bueno.spi.service.BotServiceProvider;

import java.util.ArrayList;
import java.util.List;

public class SabotaBot implements BotServiceProvider {

    @Override
    public boolean getMaoDeOnzeResponse(GameIntel intel) {
        return false;
    }

    @Override
    public boolean decideIfRaises(GameIntel intel) {

        List<GameIntel.RoundResult> roundResults = intel.getRoundResults();

        // nunca pedir truco na primeira rodada
        if (intel.getRoundResults().isEmpty()){
            return false;
        }

        // se ganhou a primeira rodada e tem manilha forte: truco
        if (roundResults.get(0) == GameIntel.RoundResult.WON) {
            if ( (!(getManilhasCard(intel).isEmpty())) && hasStrongManilha(intel)){
                return true;
            }
        }

        // se teve empate, o marreco jogou uma carta e temos uma mais forte: truco
        // se ganhou um dos dois primeiros rounds, o marreco jogou uma carta e temos uma mais forte: truco
        // se ganhou um dos dois primeiros rounds e temos uma manilha forte: truco
        if (
                (roundResults.contains(GameIntel.RoundResult.DREW)) ||
                (roundResults.contains(GameIntel.RoundResult.WON)))
        {
            if (canRise(intel)){
                return true;
            }
        }

        // se ganhamos a primeira e podemos empatar: truco
        if( (roundResults.size() > 1) && (intel.getRoundResults().get(0) == GameIntel.RoundResult.WON) ){
            if (intel.getOpponentCard().isPresent()) {
                return opponentHasTheSameCard(intel, intel.getOpponentCard().get());
            }
        }
        return false;
    }

    @Override
    public CardToPlay chooseCard(GameIntel intel) {
        var hand = intel.getCards();
        var opponentCard = intel.getOpponentCard();
        var roundResults = intel.getRoundResults();

        // se for o primeiro round
        if (roundResults.isEmpty()) {
            // se for o primeiro a jogar
            if (opponentCard.isEmpty()) return getCardBeingFirstToPlay(intel, hand);
            // resposta à jogada do adversário
            else return getResponseToOpponent(intel, hand, opponentCard.get());
        } else if (roundResults.get(0) == GameIntel.RoundResult.DREW){
            return CardToPlay.of(getGreatestCard(intel, hand));
        } else if (roundResults.get(0) == GameIntel.RoundResult.WON){
            if (hasStrongCards(hand))
                return CardToPlay.discard(getWeakestCard(intel, hand));
        }
        return CardToPlay.of(hand.get(0));
    }

    @Override
    public int getRaiseResponse(GameIntel intel) {

        List<GameIntel.RoundResult> roundResults = intel.getRoundResults();
        List<TrucoCard> manilhas = getManilhasCard(intel);

        if (!manilhas.isEmpty()){
            if (manilhas.size() >= 2){
                return 0;
            }

            if (getZap(intel) != null){
                return 1;
            }
        }

        if (intel.getOpponentScore() == 11){
            return -1;
        }


        return -1;
    }



    // general functions

    // manilhas getters
    private TrucoCard getZap(GameIntel intel){

        List<TrucoCard> manilhas = getManilhasCard(intel);

        if (manilhas.size() >= 1){
            for (TrucoCard card : manilhas) {
                if (card.isZap(intel.getVira())){
                    return card;
                }
            }
        }
        return null;
    }

    private TrucoCard getCopas(GameIntel intel){

        List<TrucoCard> manilhas = getManilhasCard(intel);

        if (manilhas.size() >= 1){
            for (TrucoCard card : manilhas) {
                if (card.isCopas(intel.getVira())){
                    return card;
                }
            }
        }
        return null;
    }

    private TrucoCard getSpadilha(GameIntel intel){

        List<TrucoCard> manilhas = getManilhasCard(intel);

        if (manilhas.size() >= 1){
            for (TrucoCard card : manilhas) {
                if (card.isEspadilha(intel.getVira())){
                    return card;
                }
            }
        }
        return null;
    }

    private TrucoCard getOuro(GameIntel intel){

        List<TrucoCard> manilhas = getManilhasCard(intel);

        if (manilhas.size() >= 1){
            for (TrucoCard card : manilhas) {
                if (card.isOuros(intel.getVira())){
                    return card;
                }
            }
        }
        return null;
    }

    private List<TrucoCard> getManilhasCard(GameIntel intel){
        List<TrucoCard> manilhas = new ArrayList<>();
        for (TrucoCard card : intel.getCards())
            if (card.isManilha(intel.getVira()))
                manilhas.add(card);
        return manilhas;
    }

    private boolean hasStrongManilha(GameIntel intel) {
        TrucoCard vira = intel.getVira();
        List<TrucoCard> manilhas = getManilhasCard(intel);
        for (TrucoCard card : manilhas)
            if (card.isCopas(vira) || card.isZap(vira))
                return true;
        return false;
    }



    // functions for normal cards
    private List<TrucoCard> getNonManilhas(List<TrucoCard> hand, TrucoCard vira) {
        List<TrucoCard> nonManilhas = new ArrayList<>();
        for (TrucoCard card : hand) {
            if (!card.isManilha(vira))
                nonManilhas.add(card);
        }
        return nonManilhas;
    }

    private TrucoCard getWeakestCard(GameIntel intel, List<TrucoCard> hand) {
        if (hand.size() == 3) {
            if (hand.get(0).compareValueTo(hand.get(1), intel.getVira()) < 0) {
                if (hand.get(0).compareValueTo(hand.get(2), intel.getVira()) < 0)
                    return hand.get(0);
                else
                    return hand.get(2);
            } else if (hand.get(1).compareValueTo(hand.get(2), intel.getVira()) < 0)
                return hand.get(1);
            else
                return hand.get(2);
        } else if (hand.size() == 2) {
            if (hand.get(0).compareValueTo(hand.get(1), intel.getVira()) < 0)
                return hand.get(0);
            else
                return hand.get(1);
        }
        return hand.get(0);
    }

    private boolean hasStrongCardsNonManilha(GameIntel intel, List<TrucoCard> hand) {
        int KING_VALUE = 7;
        for (TrucoCard card : hand)
            if (card.getRank().value() > KING_VALUE && !card.isManilha(intel.getVira()))
                return true;
        return false;
    }

    private boolean hasStrongCards(List<TrucoCard> hand) {
        int TWO_VALUE = 9;
        for (TrucoCard card : hand)
            if (card.getRank().value() >= TWO_VALUE)
                return true;
        return false;
    }

    private TrucoCard getGreatestCard(GameIntel intel, List<TrucoCard> hand) {
        if (hand.size() == 3) {
            if (hand.get(0).compareValueTo(hand.get(1), intel.getVira()) > 0) {
                if (hand.get(0).compareValueTo(hand.get(2), intel.getVira()) > 0)
                    return hand.get(0);
                else
                    return hand.get(2);
            } else if (hand.get(1).compareValueTo(hand.get(2), intel.getVira()) > 0)
                return hand.get(1);
            else
                return hand.get(2);
        } else if (hand.size() == 2) {
            if (hand.get(0).compareValueTo(hand.get(1), intel.getVira()) > 0)
                return hand.get(0);
            else
                return hand.get(1);
        }
        return hand.get(0);
    }



    // functions related to the opponent and the game
    private Integer getDiferencePoints(GameIntel intel){
        return intel.getScore() - intel.getOpponentScore();
    }

    private boolean opponentHasStrongCard(GameIntel intel, TrucoCard opponentCard) {
        for (TrucoCard card : intel.getCards())
            if (card.compareValueTo(opponentCard, intel.getVira()) > 0)
                return false;
        return true;
    }

    private boolean opponentHasTheSameCard(GameIntel intel, TrucoCard opponentCard) {
        for (TrucoCard card : intel.getCards())
            if (card.compareValueTo(opponentCard, intel.getVira()) == 0)
                return true;
        return false;
    }



    // functions for CardToPlay
    private CardToPlay getCardBeingFirstToPlay(GameIntel intel, List<TrucoCard> hand) {
        // se tiver ouro e carta forte, já sai com ele
        for (TrucoCard card : hand) {
            if (card.isOuros(intel.getVira()) && hasStrongCardsNonManilha(intel, hand)) {
                return CardToPlay.of(card);
            }
        }

        // sai com a mais forte que não seja manilha
        return CardToPlay.of(getGreatestCard(intel, getNonManilhas(hand, intel.getVira())));
    }

    private CardToPlay getResponseToOpponent(GameIntel intel, List<TrucoCard> hand, TrucoCard opponentCard) {
        // se tiver carta igual e manilha, amarra pra jogar ela depois
        if (opponentHasTheSameCard(intel, opponentCard) &&
                !getManilhasCard(intel).isEmpty()) return getCardToDraw(intel, hand, opponentCard);

        // se o oponente jogar carta mais forte que todas, joga a menor
        if (opponentHasStrongCard(intel, opponentCard)) return CardToPlay.of(getWeakestCard(intel, hand));
            // se não, joga a maior q não seja manilha
        else return CardToPlay.of(getGreatestCard(intel, getNonManilhas(hand, intel.getVira())));
    }

    private CardToPlay getCardToDraw(GameIntel intel, List<TrucoCard> hand, TrucoCard opponentCard) {
        for (TrucoCard card : hand) {
            if (card.compareValueTo(opponentCard, intel.getVira()) == 0)
                return CardToPlay.of(card);
        }
        return CardToPlay.of(hand.get(0));
    }



    // extra functions
    private boolean canRise(GameIntel intel){
        if (intel.getOpponentCard().isPresent())
            return !opponentHasStrongCard(intel, intel.getOpponentCard().get());
        else if (!getManilhasCard(intel).isEmpty())
            return hasStrongManilha(intel);
        return false;
    }
    
}
