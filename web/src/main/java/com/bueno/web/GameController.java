/*
 *  Copyright (C) 2022 Lucas B. R. de Oliveira - IFSP/SCL
 *  Contact: lucas <dot> oliveira <at> ifsp <dot> edu <dot> br
 *
 *  This file is part of CTruco (Truco game for didactic purpose).
 *
 *  CTruco is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  CTruco is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CTruco.  If not, see <https://www.gnu.org/licenses/>
 */

package com.bueno.web;

import com.bueno.domain.usecases.game.CreateGameUseCase;
import com.bueno.domain.usecases.game.model.CreateForUserAndBotRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/game")
public class GameController {

    private final CreateGameUseCase createGameUseCase;

    public GameController(CreateGameUseCase createGameUseCase) {
        this.createGameUseCase = createGameUseCase;
    }

    @PostMapping(path = "/user_bot")
    public ResponseEntity<?> createForUserAndBot(@RequestBody CreateForUserAndBotRequest requestModel){
        final var intel = createGameUseCase.createForUserAndBot(requestModel);
        return ResponseEntity.ok(intel);
    }
}