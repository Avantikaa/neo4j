/**
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.kernel.impl.api.index;

import org.neo4j.kernel.api.index.Reservation;

class AggregatedReservation implements Reservation
{
    private final Reservation[] aggregates;
    private int pointer;

    AggregatedReservation( int size )
    {
        this.aggregates = new Reservation[size];
    }

    void add( Reservation reservation )
    {
        if ( pointer == aggregates.length )
        {
            throw new IndexOutOfBoundsException( "Too many aggregates, size = " + aggregates.length );
        }
        aggregates[pointer++] = reservation;
    }

    @Override
    public void withdraw()
    {
        Throwable firstError = null;

        for ( Reservation aggregate : aggregates )
        {
            if ( aggregate != null )
            {
                try
                {
                    aggregate.withdraw();
                }
                catch ( Throwable t )
                {
                    if ( firstError == null )
                    {
                        firstError = t;
                    }
                }
            }
        }

        if ( firstError != null )
        {
            throw new RuntimeException( firstError );
        }
    }
}
