/*
 * Copyright ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.besu.ethereum.core;

import org.hyperledger.besu.ethereum.rlp.RLPInput;
import org.hyperledger.besu.ethereum.rlp.RLPOutput;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class BlockBody implements org.hyperledger.besu.plugin.data.BlockBody {

  private static final BlockBody EMPTY =
      new BlockBody(Collections.emptyList(), Collections.emptyList());
  /**
   * Adding a new field with a corresponding root hash in the block header will require a change in
   * {@link org.hyperledger.besu.ethereum.eth.manager.task.GetBodiesFromPeerTask.BodyIdentifier }
   */
  private final List<Transaction> transactions;

  private final List<BlockHeader> ommers;
  private final Optional<List<Withdrawal>> withdrawals;

  public BlockBody(final List<Transaction> transactions, final List<BlockHeader> ommers) {
    this.transactions = transactions;
    this.ommers = ommers;
    this.withdrawals = Optional.empty();
  }

  public BlockBody(
      final List<Transaction> transactions,
      final List<BlockHeader> ommers,
      final Optional<List<Withdrawal>> withdrawals) {
    this.transactions = transactions;
    this.ommers = ommers;
    this.withdrawals = withdrawals;
  }

  public static BlockBody empty() {
    return EMPTY;
  }

  /**
   * @return The list of transactions of the block.
   */
  @Override
  public List<Transaction> getTransactions() {
    return transactions;
  }

  /**
   * @return The list of ommers of the block.
   */
  @Override
  public List<BlockHeader> getOmmers() {
    return ommers;
  }

  /**
   * Returns the withdrawals of the block.
   *
   * @return The optional list of withdrawals included in the block.
   */
  @Override
  public Optional<List<Withdrawal>> getWithdrawals() {
    return withdrawals;
  }

  /**
   * Writes Block to {@link RLPOutput}.
   *
   * @param output Output to write to
   */
  public void writeTo(final RLPOutput output) {
    output.startList();

    output.writeList(getTransactions(), Transaction::writeTo);
    output.writeList(getOmmers(), BlockHeader::writeTo);
    withdrawals.ifPresent(withdrawals -> output.writeList(withdrawals, Withdrawal::writeTo));

    output.endList();
  }

  public static BlockBody readFrom(
      final RLPInput input, final BlockHeaderFunctions blockHeaderFunctions) {
    input.enterList();
    // TODO: Support multiple hard fork transaction formats.
    final BlockBody body =
        new BlockBody(
            input.readList(Transaction::readFrom),
            input.readList(rlp -> BlockHeader.readFrom(rlp, blockHeaderFunctions)),
            input.isEndOfCurrentList()
                ? Optional.empty()
                : Optional.of(input.readList(Withdrawal::readFrom)));
    input.leaveList();
    return body;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BlockBody blockBody = (BlockBody) o;
    return Objects.equals(transactions, blockBody.transactions)
        && Objects.equals(ommers, blockBody.ommers)
        && Objects.equals(withdrawals, blockBody.withdrawals);
  }

  @Override
  public int hashCode() {
    return Objects.hash(transactions, ommers, withdrawals);
  }

  @Override
  public String toString() {
    return "BlockBody{"
        + "transactions="
        + transactions
        + ", ommers="
        + ommers
        + ", withdrawals="
        + withdrawals
        + '}';
  }
}
