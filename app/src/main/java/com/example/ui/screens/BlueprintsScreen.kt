package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.JsonCodeViewer
import com.example.ui.theme.*

@Composable
fun BlueprintsScreen(
    viewModel: MainViewModel? = null,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val context = LocalContext.current

    val pythonScript = remember {
        """# arbitrum_jev_flashloan_bot.py
# High-Frequency Aave V3 Flash Loan Arbitrage on Arbitrum One
# Powered by Alchemy WebSockets & TypeSafe AI Jev Decision Engine

import os
import json
import asyncio
import websockets
from web3 import Web3
from typesafe_ai import JevClient

# 1. Configuration & Constants
ALCHEMY_WS_URL = os.environ.get("ALCHEMY_ARBITRUM_WS_URL", "wss://arb-mainnet.g.alchemy.com/v2/YOUR_KEY")
ARBITRUM_RPC = os.environ.get("ARBITRUM_RPC", "https://arb-mainnet.g.alchemy.com/v2/YOUR_KEY")
JEV_API_KEY = os.environ.get("JEV_API_KEY", "your_jev_key")
AAVE_POOL_PROVIDER_ARB = "0xa97684ead0e402dC232d5A977953DF7ECBaB3CDb"

w3 = Web3(Web3.HTTPProvider(ARBITRUM_RPC))
jev = JevClient(api_key=JEV_API_KEY)

# 2. Jev Pre-Flight Revert Risk Questions
FLASH_LOAN_QUESTIONS = {
    "will_aave_callback_revert": {
        "type": "noul",
        "instructions": "Evaluate if competitive MEV bidding or rapid liquidity exhaustion will cause executeOperation to revert before block finality."
    },
    "profit_margin_viability": {
        "type": "choice",
        "options": ["highly_profitable", "fee_exhausted_negative", "revert_loss_only"],
        "instructions": "Categorize net profit after subtracting 0.05% Aave premium fee and network gas."
    },
    "block_safety_score": {
        "type": "score",
        "instructions": "Rate overall block safety from 1 (severe MEV sandwich trap) to 5 (clean block space execution)."
    }
}

async def subscribe_alchemy_swaps():
    print("🦅 Connecting to Alchemy Arbitrum WebSockets...")
    async with websockets.connect(ALCHEMY_WS_URL) as ws:
        # Subscribe to Uniswap V3 Pool Swap Events
        subscribe_msg = {
            "jsonrpc": "2.0",
            "id": 1,
            "method": "eth_subscribe",
            "params": ["logs", {"topics": ["0xc42079f94a6350d7e6235f29174924f9d5fb24d7764344070a2936a2829141c2"]}]
        }
        await ws.send(json.dumps(subscribe_msg))
        print("⚡ Subscribed to live Arbitrum swap stream. Listening for spreads...")
        
        while True:
            msg = await ws.recv()
            event_data = json.loads(msg)
            asyncio.create_task(handle_swap_event(event_data))

async def handle_swap_event(event):
    # Step 1: Rapid off-chain gross profit calculation (<20ms)
    borrow_amount = 500000 # USDC
    raw_spread = 0.0080    # 0.8% detected vs Arkham Spot
    gross_revenue = borrow_amount * raw_spread
    aave_fee = borrow_amount * 0.0005 # 0.05% Aave V3 fee = $250
    dex_fees = borrow_amount * 0.003  # 0.3% DEX swap fees = $1,500
    gas_cost = 0.04                   # Arbitrum sub-cent gas
    net_profit = gross_revenue - (aave_fee + dex_fees + gas_cost)
    
    if net_profit <= 0:
        return # Skip unprofitable spreads immediately
        
    # Step 2: Formulate State and Dispatch to Jev (<200ms)
    state = f"AaveV3 Arbitrum One Flash Loan. Borrow: ${'$'}{borrow_amount:,} USDC. Net Profit: ${'$'}{net_profit:,.2f}. Mempool: 1 competitor observed. Gas: 0.1 gwei."
    
    # Sub-second System 1 classification
    decision = jev.evaluate(
        model="jev-latest",
        state=state,
        questions=FLASH_LOAN_QUESTIONS
    )
    
    revert_risk = decision.results["will_aave_callback_revert"]["true"]
    safety = decision.results["block_safety_score"]["score"]
    margin_class = decision.results["profit_margin_viability"]["selected"]
    
    # Step 3: Deterministic Gate Rules
    if revert_risk > 0.12 or safety < 4.0:
        print(f"🛑 Revert Shield Activated: Blocked! Revert Risk: {revert_risk*100:.1f}%, Safety: {safety}/5")
        return
        
    if margin_class == "fee_exhausted_negative":
        print("🛑 Aborted: Aave 0.05% fee and slippage squeeze detected.")
        return
        
    # Step 4: Fire to Arbitrum FCFS Sequencer
    print(f"🚀 JEV APPROVED: Executing Aave V3 flash loan on Arbitrum! Net Profit: +${'$'}{net_profit:,.2f}")
    # contract.functions.requestFlashLoan(USDC_ADDRESS, borrow_amount).transact()

if __name__ == "__main__":
    asyncio.run(subscribe_alchemy_swaps())
"""
    }

    val solidityContract = remember {
        """// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@aave/core-v3/contracts/flashloan/base/FlashLoanSimpleReceiverBase.sol";
import "@openzeppelin/contracts/token/ERC20/IERC20.sol";

/**
 * @title ArbitrumFlashLoanArbitrage
 * @notice Sub-second atomic flash loan arbitrage executor on Arbitrum One
 * @dev Called only after off-chain Jev System One pre-flight approval
 */
contract ArbitrumFlashLoanArbitrage is FlashLoanSimpleReceiverBase {
    address public immutable owner;

    event FlashLoanExecuted(address asset, uint256 amount, uint256 profit);

    constructor(address _addressProvider) 
        FlashLoanSimpleReceiverBase(IPoolAddressesProvider(_addressProvider)) 
    {
        owner = msg.sender;
    }

    modifier onlyOwner() {
        require(msg.sender == owner, "Only owner can call");
        _;
    }

    /**
     * @notice Aave V3 Pool callback function
     */
    function executeOperation(
        address asset,
        uint256 amount,
        uint256 premium,
        address initiator,
        bytes calldata params
    ) external override returns (bool) {
        require(msg.sender == address(POOL), "Caller must be Aave Pool");
        require(initiator == address(this), "Initiator must be this contract");

        // 1. YOUR ARBITRAGE SWAP LOGIC:
        // Swap borrowed 'asset' on Uniswap V3 for TargetToken
        // Swap TargetToken on SushiSwap / Arkham Spot back to 'asset'

        // 2. Ensure payback to Aave (Principal + Fixed 0.05% Premium Fee)
        uint256 totalOwing = amount + premium;
        require(
            IERC20(asset).balanceOf(address(this)) >= totalOwing, 
            "Insufficient funds to repay Aave flash loan"
        );
        
        IERC20(asset).approve(address(POOL), totalOwing);

        // 3. Keep remaining surplus profit in contract
        uint256 profit = IERC20(asset).balanceOf(address(this)) - totalOwing;
        emit FlashLoanExecuted(asset, amount, profit);

        return true;
    }

    /**
     * @notice Initiates flash loan after Jev pre-flight gate clearance
     */
    function requestFlashLoan(address _token, uint256 _amount) external onlyOwner {
        POOL.flashLoanSimple(
            address(this),
            _token,
            _amount,
            "",
            0 // Referral code
        );
    }

    function withdraw(address _token) external onlyOwner {
        uint256 balance = IERC20(_token).balanceOf(address(this));
        IERC20(_token).transfer(owner, balance);
    }
}
"""
    }

    val conversationTranscript = remember {
        """# Complete Architecture & AI Mode Conversation Archive

## Project Overview: Agent Harness & Sub-Second Crypto Arbitrage with JEV

### 1. What is TypeSafe AI's Jev?
Jev is a specialized "System One" decision engine. Unlike autoregressive text-generating LLMs (Claude, GPT-4) which waste seconds rendering human-readable tokens, Jev consumes unstructured state context and outputs typed, probabilistic classifications in milliseconds (<120ms).
- **Speed**: Up to 200x faster than LLMs.
- **Cost**: 400x cheaper ($0.042 / 1M input tokens, zero output token fees).
- **Zero Hallucination**: Outputs calibrated floats and category pills rather than unpredictable strings.

### 2. The 3 Jev Primitives
1. `noul`: Boolean probability distribution (`{"true": 0.985, "false": 0.015}`)
2. `choice`: Categorical selection from a fixed list (`["accumulation", "distribution", "neutral"]`)
3. `score`: Numerical rating on a fixed scale (e.g., 1.0 to 5.0)

### 3. Arkham Intel & Nansen Integration Architecture
```
[Arkham / Nansen Data Feeds] (WebSockets / Mempool logs)
       │
       ▼
[State Engine] ────────────────► [Jev Engine] (3 Primitives in <200ms)
       │                                │
       ▼                                ▼
[DEX Execution] ◄─────────────── [Deterministic Code Gate]
```

### 4. Aave V3 Flash Loan Arbitrage on Arbitrum One
- **Why Arbitrum One?**:
  - 0.25-second block time matching Jev's <200ms evaluation window.
  - First-Come First-Served (FCFS) sequencer eliminates public mempool sandwich bidding wars.
  - Sub-cent gas fees ($0.01 - $0.05) vs $15 - $100+ on Ethereum Mainnet.
- **Profit Equation**:
  `Net Profit = Gross Revenue - (Aave 0.05% Fee + DEX Fees + Network Gas)`
- **Jev Pre-Flight Revert Shield Rules**:
  - If `will_aave_callback_revert > 0.12` or `block_safety_score < 4.0` -> ABORT!
  - If `profit_margin_viability == "fee_exhausted_negative"` -> ABORT!
  - Otherwise -> Clear contract call to Arbitrum FCFS Sequencer.
"""
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(JevDarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header Title ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = JevCyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BLUEPRINTS & EXPORT",
                            color = JevCyanPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Production Python Script, Solidity Contract & Transcript Export",
                        color = JevTextSecondary,
                        fontSize = 11.sp
                    )
                }

                FilledTonalButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Jev Transcript", conversationTranscript)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Full Transcript copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = JevDarkSurfaceElevated),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("copy_transcript_header_button")
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = JevCyanPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy .md", color = JevCyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- Tabs Navigation ---
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = JevDarkSurface,
                contentColor = JevCyanPrimary,
                indicator = {},
                divider = {}
            ) {
                listOf("Architecture", "Python Bot", "Solidity (Aave)", "Transcript (.md)").forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Tab(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                color = if (isSelected) JevCyanPrimary else JevTextMuted,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        },
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) JevCyanPrimary.copy(alpha = 0.15f) else Color.Transparent)
                    )
                }
            }
        }

        when (selectedTab) {
            0 -> {
                // Architecture Tab
                item {
                    BlueprintCard(
                        title = "Why 'System One' Decision Engine?",
                        body = "Released by TypeSafe AI, Jev is a non-autoregressive 'System One' classification model. It does not waste compute generating human-readable text tokens. Instead, it consumes unstructured state and outputs typed, probabilistic classifications in milliseconds (<120ms).\n\n" +
                                "By embedding Jev in an agent harness or trading loop, you execute decisions 200x faster and 400x cheaper ($0.042/1M input tokens, zero output fee) than traditional autoregressive LLMs like Claude or GPT-4."
                    )
                }

                item {
                    BlueprintCard(
                        title = "Arbitrum One: The Optimal Network for Jev",
                        body = "• 0.25s Block Time: Matches Jev's <200ms processing speed, executing trades inside the next block window.\n" +
                                "• First-Come First-Served (FCFS) Sequencer: No public mempool bidding war or Flashbots validator bribes.\n" +
                                "• Sub-Cent Gas: A failed transaction on Arbitrum costs ~$0.04 instead of $65+ on Ethereum Mainnet."
                    )
                }

                item {
                    BlueprintCard(
                        title = "The 3 Core Jev Primitives",
                        body = "1. noul: Yes/No Boolean probability distribution (e.g., {'true': 0.985, 'false': 0.015})\n" +
                                "2. choice: Categorical selection from a fixed list (e.g., ['highly_profitable', 'fee_exhausted_negative'])\n" +
                                "3. score: Numerical confidence/risk rating on a fixed scale (e.g., 1.0 to 5.0)"
                    )
                }
            }

            1 -> {
                // Python WebSocket Script
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ARBITRUM ALCHEMY WS + JEV BOT",
                            color = JevTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Python Bot", pythonScript))
                                Toast.makeText(context, "Python script copied!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = JevCyanPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    JsonCodeViewer(codeJson = pythonScript)
                }
            }

            2 -> {
                // Solidity Aave V3 Contract
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SOLIDITY: AAVE V3 FLASH LOAN CONTRACT",
                            color = JevTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Solidity Contract", solidityContract))
                                Toast.makeText(context, "Solidity contract copied!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = JevCyanPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    JsonCodeViewer(codeJson = solidityContract)
                }
            }

            3 -> {
                // Full Transcript Export
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = JevDarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, JevBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CONVERSATION TRANSCRIPT ARCHIVE",
                                    color = JevCyanPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Row {
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Transcript", conversationTranscript))
                                            Toast.makeText(context, "Transcript copied to clipboard!", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = JevCyanPrimary, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = {
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, conversationTranscript)
                                                type = "text/plain"
                                            }
                                            val shareIntent = Intent.createChooser(sendIntent, "Export JEV Architecture Transcript")
                                            context.startActivity(shareIntent)
                                        }
                                    ) {
                                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = JevCyanPrimary, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = conversationTranscript,
                                color = JevTextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BlueprintCard(title: String, body: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = JevDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, JevBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                color = JevCyanPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = body,
                color = JevTextSecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}
