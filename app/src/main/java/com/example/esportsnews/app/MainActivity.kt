/* Esports Hub - Plataforma de acompanhamento de e-sports
Copyright (C) 2026 George Filipe Rodrigues de Lacerda

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>. */

package com.example.esportsnews.app

import com.example.esportsnews.feature.matchdetail.MatchDetailViewModel
import com.example.esportsnews.feature.matches.MatchesViewModel
import com.example.esportsnews.feature.updates.AppUpdatesViewModel

import android.os.Bundle
import android.graphics.Color
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val viewModel: MatchesViewModel by viewModels()

    private val detailViewModel: MatchDetailViewModel by viewModels()
    private val updatesViewModel: AppUpdatesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent { EsportsApp(viewModel, detailViewModel, updatesViewModel) }
    }
}
